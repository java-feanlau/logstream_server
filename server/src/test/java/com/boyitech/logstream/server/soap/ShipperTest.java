package com.boyitech.logstream.server.soap;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.server.BaseTest;
import com.boyitech.logstream.server.factory.SingleManagerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;


/**
 * @author Eric
 * @Title: CreateShipper
 * @date 2019/4/9 17:11
 * @Description: Shipper的创建，启动，暂停，销毁soap接口测试
 */

public class ShipperTest extends BaseTest {
    private String cacheLv1;
    private String shipperConfig;

    @Test
    public void testFile() {
        //language=JSON
        shipperConfig = "{\"moduleType\": \"file\",\"index\": \"text_ys\",\"readPath\": [\"/Users/juzheng/Downloads/工作文件夹/普通日志文件及相关/vpn.txt\"],\"fileNameMatch\": \"vpn.txt\",\"threadPollMax\": \"1\",\"ignoreOld\": \"false\",\"ignoreFileOfTime\": \"86400\",\"saveOffsetTime\": \"5\",\"encoding\": \"utf8\",\"secondOfRead\": \"5\"}";
    }

    @Test
    public void testRedis() {
        shipperConfig = "{" +
                "\"moduleType\": \"redis\"," +
                "\"index\": \"text_redis\"," +
                "\"host\": \"172.17.30.10\"," +
                "\"port\": \"6379\"," +
                "\"passwd\": \"d115deabc61ac79fc179dd75efaf720f519d35b0ef7c2d473c74b32c4877eca6\"," +
                "\"keys\": [\"a:b:c\"]," +
                "\"DBindex\": \"0\"" +
                "}";

    }

    @Test
    public void testSyslog() {
        shipperConfig = "{" +
                "\"moduleType\": \"syslog\"," +
                "\"index\": \"test_syslog\"," +
                "\"host\": \"192.168.144.1\"," +
                "\"port\": \"515\"," +
                "\"protocol\": \"udp\"" +
                "}";

    }

    @Before
    public void testCreateCache() {
        cacheLv1 = instance.createCacheLv1();
    }

    @After
    public void testCreateShipper() throws InterruptedException {
        BaseCache lv1CacheById =  SingleManagerFactory.getCacheManager().getLv1Cache(cacheLv1);
        //创建
        String soapCreate = instance.createShipperWorker(shipperConfig, cacheLv1);
        Map<String, String> createMap = GsonHelper.fromJson(soapCreate);
        String createStatus = createMap.get("soap_status");
        String shipperID = createMap.get("shipperID");
        assertEquals("200", createStatus);
        //启动
        String soapStart = instance.startShipperWorker(shipperID);
        Map<String, String> startMap = GsonHelper.fromJson(soapStart);
        String startStatus = startMap.get("soap_status");
        assertEquals("200", startStatus);

        //暂停
        String soapStop = instance.stopShipperWorker(shipperID);
        Map<String, String> stopMap = GsonHelper.fromJson(soapStop);
        String stopStatus = stopMap.get("soap_status");
        assertEquals("200",stopStatus);
        //销毁
        String soapdestroy = instance.destroyShipperWorker(shipperID);
        Map<String, String> destroyMap = GsonHelper.fromJson(soapdestroy);
        String destroyStatus = destroyMap.get("soap_status");
        assertEquals("200",destroyStatus);


    }
}
