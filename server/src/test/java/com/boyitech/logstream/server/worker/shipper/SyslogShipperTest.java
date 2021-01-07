package com.boyitech.logstream.server.worker.shipper;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.factory.CacheFactory;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.setting.StatsSettings;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.porter.BasePorterConfig;
import com.boyitech.logstream.core.worker.porter.syslog.SyslogPorter;
import com.boyitech.logstream.core.worker.porter.syslog.SyslogPorterConfig;
import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;
import com.boyitech.logstream.core.worker.shipper.syslog_old.SyslogShipper;
import com.boyitech.logstream.core.worker.shipper.syslog_old.SyslogShipperConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Eric
 * @Title: FileShipperTest
 * @date 2019/4/10 16:59
 * @Description: TODO
 */
public class SyslogShipperTest {

    private Map shipperConfig;
    private String valueOfInput;

    @Before
    public void testPrepare() {
        //language=JSON
        String shipperConfig = "{" +
                "\"moduleType\": \"syslog\"," +
                "\"index\": \"test_syslog\"," +
                "\"host\": \"172.17.20.81\"," +
                "\"port\": \"8081\"," +
                "\"protocol\": \"udp\"" +
                "}";
        this.shipperConfig = GsonHelper.fromJson(shipperConfig);
        valueOfInput = UUID.randomUUID().toString();
    }


    @Test
    public void recevied() throws InterruptedException {

        BaseShipperConfig config = new SyslogShipperConfig(shipperConfig);
        SyslogShipper syslogShipper = new SyslogShipper(config);
        BaseCache cache = CacheFactory.createCache();
        syslogShipper.setLv1Cache(cache);
        syslogShipper.doStart();
        BaseCache cache1 = CacheFactory.createCache();
        for (int i = 0; i < 1000; i++) {
            Event event = new Event();
            event.setMessage("219.220.241.137 - - [04/Dec/2018:15:43:52 +0800] \"GET /epstar/web/applications/HRMS/NDKHGL/GRTB/GRZBZP/index.jsp?l=002&current.model.id=5c4o136-qyn2no-g6wz3jau-1-g6wzectu-k HTTP/1.1\" 200 2861\n");
            Map map = new HashMap<>();
            map.put("test", "test");
            map.put("message", event.getMessage());
            event.setFormat(map);
            event.setIndex("test514");
            event.setLogType("nginx_success");
            cache1.offer(event);
        }


        String porterConfig = "{" +
                "\"moduleType\": \"syslog\"," +
                "\"ip\": \"172.17.20.53\"," +
                "\"port\": \"515\"" +
                "}";
        Map<String, String> map1 = GsonHelper.fromJson(porterConfig);
        BasePorterConfig config1 = new SyslogPorterConfig(map1);
        SyslogPorter syslogPorter = new SyslogPorter(config1);
        syslogPorter.setLv3Cache(cache1);
        syslogPorter.doStart();

        int i = 1;
        while (syslogShipper.getCount().get() == 0 && i != 10) {
            Thread.sleep(1000);
            i++;
        }
        if(i>=10){
            Assert.assertTrue(false);
        }


    }

    //测试
    @Test
    public void testStop() throws FileNotFoundException, UnsupportedEncodingException, InterruptedException {
        String shipperConfig = "{" +
                "\"moduleType\": \"syslog\"," +
                "\"index\": \"test_syslog\"," +
                "\"host\": \"172.17.20.53\"," +
                "\"port\": \"514\"," +
                "\"protocol\": \"udp\"" +
                "}";
        Map<String, String> configMap = GsonHelper.fromJson(shipperConfig);
        BaseShipperConfig config = new SyslogShipperConfig(configMap);
        SyslogShipper syslogShipper = new SyslogShipper(config);
        BaseCache cache = CacheFactory.createCache();
        syslogShipper.setLv1Cache(cache);
        syslogShipper.doStart();
        System.out.println(syslogShipper.isAlive());
//        syslogShipper.doStop();
        Thread.sleep(Integer.parseInt(StatsSettings.STATS_QUERY_SLEEP_TIME.getValue()));
        System.out.println(syslogShipper.isAlive());
        syslogShipper.doStart();
        Thread.sleep(Integer.parseInt(StatsSettings.STATS_QUERY_SLEEP_TIME.getValue()));
        System.out.println(syslogShipper.isAlive());

    }

}
