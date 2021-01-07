package com.boyitech.logstream.server.soap;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.setting.StatsSettings;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.server.BaseTest;
import com.boyitech.logstream.server.factory.SingleManagerFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Eric
 * @Title: StatsTest
 * @date 2019/4/10 16:04
 * @Description: TODO
 */
public class StatsTest extends BaseTest {


    //测试所有worker状态的获取情况
    @Test
    public void testGetAllWorkersStats() throws InterruptedException {
        String config = "{" +
                "\"logType\": \"nginx_success\"" +
                "}";
        String indexerWorker = instance.createIndexerWorker(config, instance.createCacheLv1(), instance.createCacheLv2());
        String indexerID = GsonHelper.fromJson(indexerWorker).get("indexerID");
        String allWorkersStats1 = instance.getAllWorkersStats();
        while (!allWorkersStats1.equals("{}")) {
            String createStatus = GsonHelper.fromJson(allWorkersStats1).get(indexerID);
            assertEquals("400", createStatus);
        }

        instance.startIndexerWorker(indexerID);
        String allWorkersStats2 = instance.getAllWorkersStats();
        String startStatus = GsonHelper.fromJson(allWorkersStats2).get(indexerID);
        Assert.assertEquals("200", startStatus);
        instance.destroyIndexerWorker(indexerID);
        String allWorkersStats3 = instance.getAllWorkersStats();
        Assert.assertEquals("{}", allWorkersStats3);
    }


    //测试获取指定worker状态的获取情况
    @Test
    public void testGetWorkerStatsByKey() throws InterruptedException {
        String config = "{" +
                "\"logType\": \"nginx_success\"" +
                "}";
        String indexerWorker = instance.createIndexerWorker(config, instance.createCacheLv1(), instance.createCacheLv2());
        Thread.sleep(Integer.parseInt(StatsSettings.STATS_QUERY_SLEEP_TIME.getValue()));
        String indexerID = GsonHelper.fromJson(indexerWorker).get("indexerID");
        String allWorkersStats1 = instance.getWorkerStatsByKey(indexerID);
        assertEquals("400", allWorkersStats1);
        instance.startIndexerWorker(indexerID);
        Thread.sleep(Integer.parseInt(StatsSettings.STATS_QUERY_SLEEP_TIME.getValue()));
        String allWorkersStats2 = instance.getWorkerStatsByKey(indexerID);
        Assert.assertEquals("200", allWorkersStats2);
        instance.destroyIndexerWorker(indexerID);
        Thread.sleep(Integer.parseInt(StatsSettings.STATS_QUERY_SLEEP_TIME.getValue()));
        String allWorkersStats3 = instance.getWorkerStatsByKey(indexerID);
        Assert.assertNull(allWorkersStats3);
    }


    //获取所有cache状态的情况
    @Test
    public void testGetAllCachesStats() throws InterruptedException {
        instance.createCacheLv1();
        instance.createCacheLv2();
        instance.createCacheLv3();
        Thread.sleep(Integer.parseInt(StatsSettings.STATS_QUERY_SLEEP_TIME.getValue()));

        String allCachesStats = instance.getAllCachesStats();
        Map<String, String> stringStringMap = GsonHelper.fromJson(allCachesStats);
        Iterator<String> it = stringStringMap.values().iterator();
        if (it.hasNext()) {
            Assert.assertEquals("0", it.next());
        }else {
            Assert.assertTrue(false);
        }
    }

    //测试获取指定worker状态的获取情况
    @Test
    public void testGetCachesStatsByKey() throws InterruptedException {
        String cacheLv1 = instance.createCacheLv1();
        Thread.sleep(Integer.parseInt(StatsSettings.STATS_QUERY_SLEEP_TIME.getValue()));
        String cacheStatsByKey = instance.getCacheStatsByKey(cacheLv1);
        Assert.assertEquals("0", cacheStatsByKey);
    }

    //测试速度
    @Test
    public void testSpeed() throws InterruptedException {
//        Settings.load();
        String shipperConfig = "{" +
                "\"moduleType\": \"file\"," +
                "\"index\": \"text_ys\"," +
                "\"readPath\": [\"C:/Users/Eric/Desktop/testShipper\"]," +
                //"\"fileNameMatch\": \"1*.txt\"," +
                "\"threadPollMax\": \"1\"," +
                "\"ignoreOld\": \"false\"," +
                "\"ignoreFileOfTime\": \"86400\"," +
                "\"saveOffsetTime\": \"5\"," +
                "\"encoding\": \"utf8\"," +
                "\"secondOfRead\": \"5\"" +
                "}";

        String cacheLv1 = instance.createCacheLv1();
        BaseCache lv1CacheById = SingleManagerFactory.getCacheManager().getLv1Cache(cacheLv1);
        String soapCreate = instance.createShipperWorker(shipperConfig, cacheLv1);
        Map<String, String> createMap = GsonHelper.fromJson(soapCreate);
        String createStatus = createMap.get("soap_status");
        String shipperID = createMap.get("shipperID");
        assertEquals("200", createStatus);
        //启动
        instance.startShipperWorker(shipperID);
        Assert.assertNotNull(instance.getShipperWorkerSpeed(shipperID));

    }

}
