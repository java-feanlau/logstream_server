package com.boyitech.logstream.server.worker.indexer;

import com.alibaba.fastjson.JSONObject;
import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.factory.CacheFactory;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.manager.indexer.BaseIndexerManager;
import com.boyitech.logstream.core.setting.Settings;
import com.boyitech.logstream.core.setting.WorkerSettings;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import com.boyitech.logstream.core.worker.porter.BasePorterConfig;
import com.boyitech.logstream.core.worker.porter.elasticsearch.ElasticsearchPorter;
import com.boyitech.logstream.core.worker.porter.elasticsearch.ElasticsearchPorterConfig;
import com.boyitech.logstream.server.BaseTest;
import com.boyitech.logstream.worker.indexer.*;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class IndexerPorterEKTest  {
    private BaseCache LV2cache;
    private String comName="boyi_test";
    private String indexerName="nginx_nginx_error_v1";
    private String FILEPATH="/Users/juzheng/Downloads/boyi/普通日志文件及相关/nginxError.txt";
    @Before
    public void buildEventTest() throws InterruptedException {
        LV2cache = CacheFactory.createCache();
        Map<String, String> map = new HashMap();
        map.put("logType", indexerName);
        BaseIndexerConfig baseIndexerConfig = new BaseIndexerConfig(map);

        BaseIndexer indexer = new NginxNginxErrorV1Indexer(baseIndexerConfig);
        indexer.register();
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(new File(FILEPATH)));
            String s=new String() ;
            while ((s = br.readLine()) != null)
            {
                Event e = new Event();
                e.setMessage(s);
                e.setIndex(comName+"_"+indexerName);
                e.setLogType(indexerName);
                indexer.format(e);
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
        BaseIndexerManager.putMapping(indexerName, NginxNginxErrorV1Indexer.class);
        elasticSearchPorter.setLv2Cache(LV2cache);
        elasticSearchPorter.setLv3Cache(null);
        elasticSearchPorter.doStart();
        while (true){
        Thread.sleep(1000);
        }
    }

}