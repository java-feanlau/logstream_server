package com.boyitech.logstream.server.worker.porter;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.factory.CacheFactory;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.manager.indexer.BaseIndexerManager;
import com.boyitech.logstream.core.setting.WorkerSettings;
import com.boyitech.logstream.core.setting.Settings;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.porter.BasePorter;
import com.boyitech.logstream.core.worker.porter.elasticsearch.ElasticsearchPorter;
import com.boyitech.logstream.core.worker.porter.elasticsearch.ElasticsearchPorterConfig;
import com.boyitech.logstream.server.BaseTest;
import com.boyitech.logstream.worker.indexer.NginxNginxSuccessV1Indexer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eric
 * @Title: ElasticSearchPorter
 * @date 2019/4/19 14:11
 * @Description: TODO
 */
public class ElasticSearchPorterTest extends BaseTest {

    private Event event;

    @Before
    public void buildEventTest() {
        event = new Event();
        event.setMessage("12312");
        Map map = new HashMap<>();
        map.put("test", "test");
        event.setFormat(map);
        event.setIndex("zh_test123");
        event.setLogType("nginx_success");
    }

    //是否能正确创建索引，以及是否能正确上传mapping和写入数据
    @Test
    public void testESPorter() throws InterruptedException {
        Settings.load();
        String jsonConfig = "{" +
                "\"moduleType\": \"elasticsearch\"," +
                "\"ip\": \"172.17.30.11\"," +
                "\"port\": \"9300\"" +
                "}";

        Map<String, String> map = GsonHelper.fromJson(jsonConfig);
        ElasticsearchPorterConfig config = new ElasticsearchPorterConfig(map);
        BaseCache LV2cache = CacheFactory.createCache();
        LV2cache.offer(event);
        System.out.println(WorkerSettings.BATCHSIZE.getValue());
        ElasticsearchPorter elasticSearchPorter = new ElasticsearchPorter(config);
        BaseIndexerManager.putMapping("nginx_success", NginxNginxSuccessV1Indexer.class);
        elasticSearchPorter.setLv2Cache(LV2cache);
        elasticSearchPorter.setLv3Cache(null);
        elasticSearchPorter.doStart();
        while (true){

        Thread.sleep(1000);
        }
//        Assert.assertTrue(elasticSearchPorter.result);
    }

    //测试分流状态
    @Test
    public void testESPortershunk() throws InterruptedException {
        Settings.load();
        String jsonConfig = "{" +
                "\"moduleType\": \"elasticsearch\"," +
                "\"ip\": \"172.17.100.1\"," +
                "\"port\": \"9300\"" +
                "}";

        Map<String, String> map = GsonHelper.fromJson(jsonConfig);
        ElasticsearchPorterConfig config = new ElasticsearchPorterConfig(map);
        BaseCache LV2cache = CacheFactory.createCache();
        BaseCache LV3cache = CacheFactory.createCache();
        LV2cache.offer(event);
        System.out.println(WorkerSettings.BATCHSIZE.getValue());
        BasePorter elasticSearchPorter = new ElasticsearchPorter(config);
        BaseIndexerManager.putMapping("nginx_success", NginxNginxSuccessV1Indexer.class);
        elasticSearchPorter.setLv2Cache(LV2cache);
        elasticSearchPorter.setLv3Cache(LV3cache);
        elasticSearchPorter.doStart();
        Thread.sleep(WorkerSettings.BATCHSIZE.getValue());
        Assert.assertEquals(1, LV3cache.size());

    }
}
