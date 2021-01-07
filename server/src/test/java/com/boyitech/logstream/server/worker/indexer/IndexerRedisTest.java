package com.boyitech.logstream.server.worker.indexer;

import com.alibaba.fastjson.JSONObject;
import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.factory.CacheFactory;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.manager.indexer.BaseIndexerManager;
import com.boyitech.logstream.core.setting.Settings;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import com.boyitech.logstream.core.worker.porter.BasePorterConfig;
import com.boyitech.logstream.core.worker.porter.elasticsearch.ElasticsearchPorter;
import com.boyitech.logstream.core.worker.porter.elasticsearch.ElasticsearchPorterConfig;
import com.boyitech.logstream.worker.indexer.CheckpointFirewallAllV1Indexer;
import com.boyitech.logstream.worker.indexer.FortinetFirewallAllV2Indexer;
import com.boyitech.logstream.worker.indexer.SangforAccesscontrolAllV1Indexer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author juzheng
 * @Title: IndexerRedisTest
 * @date 2019/7/15 6:56 PM
 * @Description: 测试类，从redis中读取日志进行格式化的测试
 */
public class IndexerRedisTest {
    private BaseCache LV2cache;
    //    private static final String FILEPATH = "/Users/juzheng/Downloads/bosch_security_log_mapping/asiainfo-tda.txt";
    private BaseIndexer Indexer;
    private String indexerName="checkpoint_firewall_all_v1";
    private List<String> list0;

    @Before
    public void init(){
        String logtype="checkpoint-firewall";
        System.out.println("------准备进行测试，读取的是redis中："+logtype+"的数据------");
        Jedis jedis1 = new Jedis("172.17.250.200", 6379,1000000000);
        jedis1.auth("2b3c18c71d4172e698d26e2b99996eddc97f04ac343731a20516b1f1cebc7a17");
        jedis1.connect();
        jedis1.select(3);
        long llen = jedis1.llen(logtype);
        list0 = jedis1.lrange(logtype, 0, llen);
        jedis1.close();
        System.out.println("---初始化---");
        Map<String, String> map = new HashMap();
        map.put("logType", "1");
        BaseIndexerConfig baseIndexerConfig = new BaseIndexerConfig(map);
        //1.此处修改Indexer类型：如ApacheSuccessIndexer
        Indexer = new CheckpointFirewallAllV1Indexer(baseIndexerConfig);
        Indexer.register();


    }

    @Test
    public void test() throws InterruptedException {
        LV2cache = CacheFactory.createCache();
        List<Event> list = new ArrayList<>();
        for(int i=0;i<list0.size();i++){
            list.add(new Event());
            list.get(i).setMessage(list0.get(i).toString());
        }
        for (Event e:list){
            JSONObject pa=JSONObject.parseObject(e.getMessage());
            String str=pa.getString("message").replaceAll("\n", "");
            Event ee=new Event();
            ee.setMessage(str);
            ee.setIndex("boyi_test_"+indexerName);
            ee.setLogType(indexerName);
            ee.setMetafieldSource("172.17.20.81");
            Indexer.format(ee);
            LV2cache.offer(ee);
        }

        Settings.load();
        String jsonConfig = "{" +
                "\"moduleType\": \"elasticsearch\"," +
                "\"ip\": \"172.17.30.10\"," +
                "\"port\": \"9300\"" +
                "}";

        Map<String, String> map = GsonHelper.fromJson(jsonConfig);
        BasePorterConfig config = new ElasticsearchPorterConfig(map);
        ElasticsearchPorter elasticSearchPorter = new ElasticsearchPorter(config);
        BaseIndexerManager.putMapping(indexerName, CheckpointFirewallAllV1Indexer.class);
        elasticSearchPorter.setLv2Cache(LV2cache);
        elasticSearchPorter.setLv3Cache(null);
        elasticSearchPorter.doStart();
        while (true){
            Thread.sleep(1000);
        }

    }

//    @Test
//    public void testToTxt(){
//        //System.out.println(list0.size());
//        try
//        {
//            BufferedWriter br = new BufferedWriter(new FileWriter(new File(FILEPATH)));
//            int i=0;
//            while (i<list0.size())
//            {
//             Map m= (Map) JSONObject.parse(list0.get(i));
//             String message = (String) m.get("message");
//             br.write(message);
//             i++;
//            }
//            br.close();
//
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//    }

    @After
    public void AfterOne(){
        System.out.println("---测试结束---");
    }
}
