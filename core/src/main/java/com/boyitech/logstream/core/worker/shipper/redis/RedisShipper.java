package com.boyitech.logstream.core.worker.shipper.redis;

import com.alibaba.fastjson.JSONObject;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.setting.WorkerSettings;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.shipper.BaseShipper;
import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;
import com.google.gson.JsonSyntaxException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Night
 */
public class RedisShipper extends BaseShipper {

    protected Jedis jedis;
    protected List<String> args = new ArrayList<String>();
    protected RedisShipperConfig redisConfig;
    protected String scriptSha = null;
    private final String script = "local batchsize = tonumber(ARGV[1])\n"
            + "local result = redis.call('lrange', KEYS[1], 0, batchsize)\n"
            + "redis.call('ltrim', KEYS[1], batchsize + 1, -1)\n"
            + "return result";
    protected int retryTimes = 1;
    private int type;
    private String typeMessage;

    public RedisShipper(BaseShipperConfig config) {
        super(config);
        redisConfig = (RedisShipperConfig) config;
        args.add(redisConfig.getBatchSize() + "");//1000
    }

    public RedisShipper(String shipperId, BaseShipperConfig config) {
        super(shipperId, config);
        redisConfig = (RedisShipperConfig) config;
        args.add(redisConfig.getBatchSize() + "");//1000
    }

    @Override
    public boolean register() {
        type = redisConfig.getType();
        typeMessage = redisConfig.getTypeMessage();
        if(!GrokUtil.isStringHasValue(typeMessage)){
            typeMessage="message";
        }
        jedis = createJdeisClient(redisConfig.getHost(), redisConfig.getPort(), redisConfig.getDBIndex());
        if (jedis == null) {
            return false;
        }
        return true;
    }

    @Override
    public void execute() throws InterruptedException {
        List<String> redisKeys = redisConfig.getRedisKeys();
        for (String redisKey : redisKeys) {
            //每次读取之前检查redis连接是否存在
            //不存在的时候尝试重新建立连接
            if (jedis == null) {
                createJdeisClient(redisConfig.getHost(), redisConfig.getPort(), redisConfig.getDBIndex());
            }
            List<Event> eventList = new ArrayList<Event>();
            try {
                boolean flag = false;
                // 判断sha对应的脚本在redis中是否存在
                // 在高压力下redis的scriptExists接口可能返回空数组导致Jedis的查询报数组越界异常
                // 发生异常后需要重建redis链接
                try {
                    if (scriptSha == null || scriptSha.equals("")) {
                        scriptSha = jedis.scriptLoad(script);
                        flag = true;
                    } else {
                        flag = jedis.scriptExists(scriptSha);
                    }
                } catch (IndexOutOfBoundsException e) {
                    flag = false;
//					e.printStackTrace();
//					super.recordException(new Exception("scriptExists() result array's size is zero"));
                    createJdeisClient(redisConfig.getHost(), redisConfig.getPort(), redisConfig.getDBIndex());
                }
                // 脚本存在的话调用脚本读取数据
                if (flag) {
                    List<String> keys = new ArrayList();
                    keys.add(redisKey);
                    List<String> results = (List<String>) jedis.evalsha(scriptSha, keys, args);
                    for (String result : results) {
                        Event e = new Event();
                        switch (type){
                            case 0://取json指定字段，默认取message，字段其余抛弃
                                try {
                                    Map map = GsonHelper.fromJson(result);
                                    if (map.get(typeMessage) != null && !map.get(typeMessage).equals("")) {
                                        e.setMessage(map.get(typeMessage).toString());
                                    } else {
                                        e.setMessage(result);
                                    }
                                } catch (JsonSyntaxException exception) {
                                    //说明不是json格式
                                    LOGGER.error("指定字段值不是json格式，仍使用原始日志！");
                                    e.setMessage(result);
                                }
                                break;
                            case 1://取整个日志
                                e.setMessage(result);
                                break;
                            case 2://取json指定字段，默认取message，其余不抛弃
                                try {
//                                    Map map=GsonHelper.fromJson(result);
                                    Map map=JSONObject.parseObject(result);
                                    if (map.get(typeMessage) != null && !map.get(typeMessage).equals("")) {
                                        String typeMessageValue=String.valueOf(map.get(typeMessage));
                                        e.setMessage(typeMessageValue);
                                        e.setFormat(map);
                                    } else {
                                        e.setMessage(result);
                                    }
                                }
                                catch (Exception ex){
                                    LOGGER.error("指定字段值可能不是JSON格式，仍使用完整原始日志！"+ex);
                                    e.setMessage(result);
                                }
                                break;

                        }
                        e.setMsgType("redisShipper");
                        e.setSource(redisConfig.getHost());
                        e.setKey(redisKey);
                        if (this.mark != null) {
                            e.setMark(this.mark);
                        }
                        if (redisConfig.isChangeIndex()) {
                            e.setIndex(redisConfig.getIndex());
                        }
                        if (redisConfig.isChangeLogType()) {
                            e.setLogType(redisConfig.getLogType());
                        }
                        eventList.add(e);

                    }
                    // redis数据为空则休眠秒
                    if (results.isEmpty()) {
                        Thread.sleep(1000);
                    } else {
//						meter.mark(results.size());

                        count.addAndGet(eventList.size());
                    }

                    lv1Cache.put(eventList);
//					System.out.println(eventList);
                } else {
                    scriptSha = jedis.scriptLoad(script);
                }
            } catch (JedisConnectionException ex) {
                LOGGER.error(ex);
                this.addException(ex.getMessage());
                Thread.sleep(retryTimes > 300 ? 300000 : retryTimes * 1000);
                createJdeisClient(redisConfig.getHost(), redisConfig.getPort(), redisConfig.getDBIndex());
                retryTimes++;
            } catch (JedisDataException ex) {
                LOGGER.error(ex);
                this.addException(ex.getMessage());
                Thread.sleep(retryTimes > 300 ? 300000 : retryTimes * 1000);
                retryTimes++;
            }
        }
    }

    protected Jedis createJdeisClient(String host, int port, int DBIndex) {
        if (this.jedis != null) {
            this.jedis.disconnect(); //关闭旧的连接
            this.jedis = null;
        }
        Jedis jedis = new Jedis(host, port, WorkerSettings.REDISSHIPPERCONNECTMAXTIME.getValue());

        try {
            jedis.auth(redisConfig.getPasswd());
            this.jedis = jedis;
            jedis.select(DBIndex);
            retryTimes = 1;//成功验证后将retryTimes恢复至初始值
        } catch (JedisDataException e) {
            LOGGER.error(Thread.currentThread().getName(), e);
            this.addException("redis启动失败：" + e.getMessage());
            return null;
        } catch (JedisConnectionException e) {
            LOGGER.error(Thread.currentThread().getName(), e);
            this.addException("redis拒绝连接，启动失败：" + e.getMessage());
            return null;
        }
        return jedis;
    }

    @Override
    public void tearDown() {

    }

    public static boolean checkConfig() {
        // TODO Auto-generated method stub
        return false;
    }

    public static List getWorkerParametersTemplate() {
        return parametersTemplate;
    }

    private static final List parametersTemplate;

    // 构建参数模板
    static {
        parametersTemplate = new ArrayList();
        Map field1 = new HashMap();
        field1.put("fieldName", "ip");
        field1.put("displayName", "地址");
        field1.put("fieldType", "string");
        field1.put("advancedType", "ip");
        field1.put("required", true);
        field1.put("default", null);
        field1.put("range", new ArrayList());
        parametersTemplate.add(field1);
        Map field2 = new HashMap();
        field2.put("fieldName", "port");
        field2.put("displayName", "端口");
        field2.put("fieldType", "string");
        field2.put("advancedType", "port");
        field2.put("required", true);
        field2.put("default", "6379");
        field2.put("range", new ArrayList());
        parametersTemplate.add(field2);
        Map field3 = new HashMap();
        field3.put("fieldName", "password");
        field3.put("displayName", "密码");
        field3.put("fieldType", "string");
        field3.put("advancedType", "password");
        field3.put("required", true);
        field3.put("default", null);
        field3.put("range", new ArrayList());
        parametersTemplate.add(field3);
        Map field4 = new HashMap();
        field4.put("fieldName", "keys");
        field4.put("displayName", "队列名");
        field4.put("fieldType", "string");
        field4.put("advancedType", "list");
        field4.put("required", true);
        field4.put("default", null);
        field4.put("range", new ArrayList());
        parametersTemplate.add(field4);
    }

    public static void main(String args[]) throws InterruptedException {
        String json = "{\"host\":\"172.17.30.10\",\"port\":6379,\"DBindex\":\"9\",\"keys\":[\"dns\"],\"type\":\"2\",\"type_message\":\"message\",\"moduleType\":\"redis\",\"index\":\"windows_dns_all_v1-9532-\",\"passwd\":\"d115deabc61ac79fc179dd75efaf720f519d35b0ef7c2d473c74b32c4877eca6\"}";
        Map<String, String> objectObjectHashMap = GsonHelper.fromJson(json);
        RedisShipperConfig redisShipperConfig = new RedisShipperConfig(objectObjectHashMap);
        RedisShipper redisShipper = new RedisShipper(redisShipperConfig);
//        redisShipper.doStart();
        String result="{\"@timestamp\":\"2020-01-14T06:26:19.000Z\",\"severity\":6,\"message\":\"2019/12/5 13:03:50 0730 PACKET  0000001803F56190 UDP Snd 172.16.70.35    b261 R Q [8081   DR  NOERROR] A      (8)consumer(11)entitlement(5)skype(3)com(0)\",\"priority\":14,\"facility\":1,\"host\":\"172.16.200.196\",\"timestamp\":\"Jan 14 14:26:19\",\"format_level\":\"0\",\"facility_label\":\"user-level\",\"received_at\":\"2020-01-14T14:25:46+08:00\",\"severity_label\":\"Informational\",\"log_class\":\"test1\",\"logsource\":\"localhost\",\"program\":\"FirewallManager\",\"@version\":\"1\",\"type\":\"test1\"}";
        Map map=GsonHelper.fromJson(result);
        System.out.println(map.get(redisShipperConfig.getTypeMessage()).toString());

    }

}
