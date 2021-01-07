package com.boyitech.logstream.server.soap;

import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.server.BaseTest;
import org.junit.After;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;


/**
 * @author Eric
 * @Title: CreateShipper
 * @date 2019/4/9 17:11
 * @Description: Porter的创建，启动，暂停，销毁soap接口测试
 */

public class PorterTest extends BaseTest {
    private String cacheLv2;
    private String cacheLv3;
    private String porterConfig;

    @Test
    public void testElasticSearch() {
        cacheLv2 = instance.createCacheLv2();
        cacheLv3 = null;
        porterConfig = "{" +
                "\"moduleType\": \"elasticsearch\"," +
                "\"ip\": \"172.17.20.81\"," +
                "\"port\": \"9200\"" +
                "}";
        //创建
        String soapCreate = instance.createPorterWorker(porterConfig,cacheLv2,cacheLv3);
        Map<String, String> createMap = GsonHelper.fromJson(soapCreate);
        String createStatus = createMap.get("soap_status");
        String porterID = createMap.get("porterID");
        assertEquals("200",createStatus);
        //启动
        String soapStart = instance.startPorterWorker(porterID);
        Map<String, String> startMap = GsonHelper.fromJson(soapStart);
        String startStatus = startMap.get("soap_status");
        assertEquals("200",startStatus);
//        //暂停
//        String soapStop = instance.stopPorterWorker(porterID);
//        Map<String, String> stopMap = GsonHelper.fromJson(soapStop);
//        String stopStatus = stopMap.get("soap_status");
//        assertEquals("200",stopStatus);
//        //销毁
//        String soapdestroy = instance.destroyPorterWorker(porterID);
//        Map<String, String> destroyMap = GsonHelper.fromJson(soapdestroy);
//        String destroyStatus = destroyMap.get("soap_status");
//        assertEquals("200",destroyStatus);
    }

    @Test
    public void testSyslog() {
        cacheLv2 = null;
        cacheLv3 = instance.createCacheLv3();
        porterConfig = "{" +
                "\"moduleType\": \"syslog\"," +
                "\"ip\": \"172.17.30.10\"," +
                "\"port\": \"4001\"" +
                "}";
        //创建
        String soapCreate = instance.createPorterWorker(porterConfig,cacheLv2,cacheLv3);
        Map<String, String> createMap = GsonHelper.fromJson(soapCreate);
        String createStatus = createMap.get("soap_status");
        String porterID = createMap.get("porterID");
        assertEquals("200",createStatus);
        //启动
        String soapStart = instance.startPorterWorker(porterID);
        Map<String, String> startMap = GsonHelper.fromJson(soapStart);
        String startStatus = startMap.get("soap_status");
        assertEquals("200",startStatus);
        //暂停
        String soapStop = instance.stopPorterWorker(porterID);
        Map<String, String> stopMap = GsonHelper.fromJson(soapStop);
        String stopStatus = stopMap.get("soap_status");
        assertEquals("200",stopStatus);
        //销毁
        String soapdestroy = instance.destroyPorterWorker(porterID);
        Map<String, String> destroyMap = GsonHelper.fromJson(soapdestroy);
        String destroyStatus = destroyMap.get("soap_status");
        assertEquals("200",destroyStatus);
    }


    @After
    public void testCreatePorter() {


    }
}
