package com.boyitech.logstream.server.soap;

import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.server.BaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;


/**
 * @author Eric
 * @Title: CreateShipper
 * @date 2019/4/9 17:11
 * @Description: Indexer的创建，启动，暂停，销毁soap接口测试
 */

public class IndexerTest extends BaseTest {
    private String cacheLv1;
    private String cacheLv2;
    private String indexerConfig;

    @Test
    public void testNginx() {
        //language=JSON
        indexerConfig = "{" +
                "\"logType\": \"nginx_success\"" +
                "}";
    }



    @Before
    public void testCreateCache() {
        cacheLv1 = instance.createCacheLv1();
        cacheLv2 = instance.createCacheLv2();
    }

    @After
    public void testCreateIndexer() {
        //创建
        String soapCreate = instance.createIndexerWorker(indexerConfig,cacheLv1,cacheLv2);
        Map<String, String> createMap = GsonHelper.fromJson(soapCreate);
        String createStatus = createMap.get("soap_status");
        String indexerID = createMap.get("indexerID");
        assertEquals("200",createStatus);
        //启动
        String soapStart = instance.startIndexerWorker(indexerID);
        Map<String, String> startMap = GsonHelper.fromJson(soapStart);
        String startStatus = startMap.get("soap_status");
        assertEquals("200",startStatus);
        //暂停
        String soapStop = instance.stopIndexerWorker(indexerID);
        Map<String, String> stopMap = GsonHelper.fromJson(soapStop);
        String stopStatus = stopMap.get("soap_status");
        assertEquals("200",stopStatus);
        //销毁
        String soapdestroy = instance.destroyIndexerWorker(indexerID);
        Map<String, String> destroyMap = GsonHelper.fromJson(soapdestroy);
        String destroyStatus = destroyMap.get("soap_status");
        assertEquals("200",destroyStatus);

    }
}
