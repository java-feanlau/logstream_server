package com.boyitech.logstream.core.worker.porter.redis;

import com.boyitech.logstream.core.factory.CacheFactory;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.setting.WorkerSettings;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.porter.BasePorter;
import com.boyitech.logstream.core.worker.porter.BasePorterConfig;
import com.boyitech.logstream.core.worker.shipper.BaseShipper;
import com.google.gson.JsonSyntaxException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Night
 */
public class RedisPorter extends BasePorter {

    protected Jedis jedis;
    protected List<String> args = new ArrayList<String>();
    protected RedisPorterConfig redisConfig;
    protected String redisKey;
    protected String scriptSha = null;
    private final String script = "local batchsize = tonumber(ARGV[1])\n"
            + "local result = redis.call('lrange', KEYS[1], 0, batchsize)\n"
            + "redis.call('ltrim', KEYS[1], batchsize + 1, -1)\n"
            + "return result";
    protected int retryTimes = 1;

    public RedisPorter(BasePorterConfig config) {
        super(config);
        redisConfig = (RedisPorterConfig) config;
    }

    public RedisPorter(String shipperId, BasePorterConfig config) {
        super(shipperId, config);
        redisConfig = (RedisPorterConfig) config;
    }

    @Override
    public boolean register() {
        jedis = createJdeisClient(redisConfig.getHost(), redisConfig.getPort(), redisConfig.getDBIndex());
        if (jedis == null) {
            return false;
        }
        redisKey = redisConfig.getRedisKeys();
//        new Thread(() -> {
//            long befor = 0;
//            while (true) {
//                Long test = this.count.get();
//                System.out.println(test - befor);
//                befor = test;
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }).start();
        return true;
    }

    @Override
    public void execute() throws InterruptedException {
        Pipeline pipeline = jedis.pipelined();

        if (lv2Cache.size() != 0) {
            List<Event> eventList = lv2Cache.poll(1000);
            for (Event event : eventList) {
                count.addAndGet(1);
                pipeline.rpush(redisKey,event.getJsonMessage());
            }
        }
        pipeline.syncAndReturnAll();

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
        HashMap<String, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("host", "172.17.250.200");
        objectObjectHashMap.put("port", 6379);
        objectObjectHashMap.put("passwd", "2b3c18c71d4172e698d26e2b99996eddc97f04ac343731a20516b1f1cebc7a17");
        objectObjectHashMap.put("keys", "test");
        objectObjectHashMap.put("DBindex", "0");
        objectObjectHashMap.put("moduleType", "redis");

        RedisPorterConfig redisShipperConfig = new RedisPorterConfig(objectObjectHashMap);
        RedisPorter redisPorter = new RedisPorter(redisShipperConfig);
        redisPorter.setLv2Cache(CacheFactory.createCache());
        redisPorter.lv2Cache.put(new Event());
        redisPorter.doStart();

    }

}
