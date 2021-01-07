package com.boyitech.logstream.server.worker.shipper;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.factory.CacheFactory;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.shipper.netflow.NetflowShipper;
import com.boyitech.logstream.core.worker.shipper.netflow.NetflowShipperConfig;
import org.junit.Test;

/*
* @Author juzheng
* @Description netflow的测试类
* @Date 10:16 AM 2019/8/21
* @Param
* @return
*/
public class NetFlowTest {

    private  BaseCache cache;
    private String shipperConfig;


    //判断redis的数据是否正常读取并且读取的数据库号是否是配置指定的数据库号
    @Test
    public void testNetFlow() throws InterruptedException {
        //language=JSON
        shipperConfig = "{\"host\":\"172.17.20.81\",\"port\":\"8081\",\"moduleType\":netflow_v10,\"index\":\"netflow1024\",\"version\":\"10\"}";

        NetflowShipperConfig config = new NetflowShipperConfig(GsonHelper.fromJson(shipperConfig));
        NetflowShipper netflowShipper = new NetflowShipper(config);
        BaseCache cache = CacheFactory.createCache();
        netflowShipper.setLv1Cache(cache);
        netflowShipper.doStart();

       // Thread.sleep(Integer.parseInt(StatsSettings.STATS_QUERY_SLEEP_TIME.getValue()));

    }
}
