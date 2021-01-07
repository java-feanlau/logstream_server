package com.boyitech.logstream.server;

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
import com.boyitech.logstream.worker.indexer.ApacheApacheSuccessV1Indexer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author juzheng
 * @date 2020-02-14
 * @Description: 这是为了博世华域的es集群问题写的，把数据发送到es的测试类。
 */
public class TestPortApacheToES {
    private BaseCache LV2cache;
    private String comName="boshi_es_test_2020";
    private String indexerName="apapche_apache_success_v1";
    private String FILEPATH="/Users/juzheng/Desktop/1.txt";
    private  ArrayList<Event>events=new ArrayList<>();
    private long count=1;
    protected DateTimeFormatter dateTimepattern = DateTimeFormat.forPattern("YYYYMM");

    @Before
    public void buildEventTest() throws InterruptedException {
        LV2cache = CacheFactory.createCache();
        Map<String, String> map = new HashMap();
        map.put("logType", indexerName);
        BaseIndexerConfig baseIndexerConfig = new BaseIndexerConfig(map);
        BaseIndexer indexer = new ApacheApacheSuccessV1Indexer(baseIndexerConfig);
        indexer.register();
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(new File(FILEPATH)));
            String s;
            while ((s = br.readLine()) != null)
            {   count++;
                Event e = new Event();
                e.setMessage(s);
                e.setIndex(comName+"-"+indexerName);
                e.setLogType(indexerName);
                e.setSource("172.17.20.66");
                indexer.format(e);
                DateTime dt=new DateTime(e.getFormat().get("@timestamp"));
                e.setEsIndex(Integer.valueOf(dt.toString(dateTimepattern)));
                events.add(e);
                LV2cache.offer(e);
            }
            br.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //是否能正确创建索引，以及是否能正确上传mapping和写入数据
    @Test
    public void testESPorter() throws InterruptedException {
        Settings.load();
        String jsonConfig = "{" +
                "\"moduleType\": \"elasticsearch\"," +
                "\"ip\": \"172.17.30.10\"," +
                "\"port\": \"9300\"" +
                "}";

        Map<String, String> map = GsonHelper.fromJson(jsonConfig);
        BasePorterConfig config = new ElasticsearchPorterConfig(map);
        ElasticsearchPorter elasticSearchPorter = new ElasticsearchPorter(config);
        BaseIndexerManager.putMapping(indexerName, ApacheApacheSuccessV1Indexer.class);
        elasticSearchPorter.setLv2Cache(LV2cache);
        elasticSearchPorter.setLv3Cache(null);
        elasticSearchPorter.doStart();
        while (true){
            Thread.sleep(1000);
        }
    }

    @Test
    public  void test2(){
//        DateTimeFormatter dateTimepattern = DateTimeFormat.forPattern("YYYYMM");
//
//        for(Map.Entry<Integer, List<Event>> entry : map.entrySet()) {
//            System.out.println("key: " + entry.getKey() + "  value：" + entry.getValue());
//        }
//
//        System.out.println();
        List list = new ArrayList();
//        events.stream().map((e) -> {
//                   return e.getEsIndex();
//             }).distinct().forEach(System.out::println);
        Stream<Integer>stream= events.stream().map((e) -> {
                   return e.getEsIndex();
             }).distinct();
        list = stream.collect(Collectors.toList());
        for (Object l:list) {
            System.out.println((Integer)l);
        }

    }

}