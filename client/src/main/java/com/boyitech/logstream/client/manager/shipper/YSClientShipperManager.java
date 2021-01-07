package com.boyitech.logstream.client.manager.shipper;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.factory.ConfigFactory;
import com.boyitech.logstream.core.factory.WorkerFactory;
import com.boyitech.logstream.core.manager.cache.BaseCacheManager;
import com.boyitech.logstream.core.manager.shipper.BaseShipperManager;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.shipper.BaseShipper;
import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class YSClientShipperManager extends BaseShipperManager {

    @Override
    public BaseShipper createShipper(String shipperConfig, BaseCache lv1Cache) {
        if (lv1Cache == null) {
            return null;
        }
        //通过工厂类创建Worker实例
        //使用工厂类，创建出一个具体的最子类的ShipperConfig对象并且用BaseShipperConifg来引用
        Map configMap = GsonHelper.fromJson(shipperConfig);
        BaseShipperConfig baseShipperConfig = null;
        try {
            baseShipperConfig = ConfigFactory.buildShipperConfig(configMap);
        } catch (Exception e) {
            LOGGER.error("shipperConfig配置有误，shipper创建失败");
            return null;
        }
        BaseShipper worker = null;
        String shipperID = null;
        try {
            worker = WorkerFactory.createShipperWorker(baseShipperConfig);
            shipperID = worker.getWorkerId();
        } catch (Exception e) {
            LOGGER.error("ShipperWorker:" + shipperID + "创建失败");
            return null;
        }
        worker.setLv1Cache(lv1Cache);
        addShipper(worker);
        LOGGER.info("ShipperWorker:" + shipperID + "创建完成");
        return worker;
    }

    @Override
    public BaseShipper createShipper(String shipperID, String shipperConfig, BaseCache lv1cache) {
        //通过工厂类创建Worker实例
        //使用工厂类，创建出一个具体的最子类的ShipperConfig对象并且用BaseShipperConifg来引用
        Map configMap = GsonHelper.fromJson(shipperConfig);
        BaseShipperConfig baseShipperConfig = null;
        try {
            baseShipperConfig = ConfigFactory.buildShipperConfig(configMap);
        } catch (Exception e) {
            LOGGER.error("shipperConfig配置有误，shipper创建失败");
            return null;
        }
        BaseShipper worker = null;
        try {
            worker = WorkerFactory.createShipperWorker(shipperID, baseShipperConfig);
            shipperID = worker.getWorkerId();
        } catch (Exception e) {
            LOGGER.error("ShipperWorker:" + shipperID + "创建失败");
            return null;
        }
        worker.setMark(shipperID);
        worker.setLv1Cache(lv1cache);
        addShipper(worker);
        LOGGER.info("ShipperWorker:" + shipperID + "创建完成");
        return worker;
    }


    @Override
    public boolean startWorker(String key) {
        BaseShipper shipper = getShipperById(key);
        if (shipper == null)
            return false;
        shipper.doStart();
        return true;
    }

    @Override
    public boolean stopWorker(String key) {

        BaseShipper shipper = getShipperById(key);
        if (shipper == null) {
            return false;
        } else if (!shipper.isAlive()) {
            return true;
        }
        CountDownLatch countDownLatch = new CountDownLatch(shipper.getThreadsNum());
        shipper.doStop(countDownLatch);
        try {
            if (countDownLatch.await(5, TimeUnit.SECONDS)) {
                LOGGER.debug("shipper退出完成");
            } else {
                LOGGER.warn("shipper退出超时，执行中断");
                shipper.forceStop();
            }
        } catch (InterruptedException e) {
            LOGGER.error(e);
            return false;
        }
        return true;
    }


    @Override
    public boolean destroyWorker(String key) {
        //高：删除的时候如果如果不存该worker，希望返回的是true。

        BaseShipper shipper = getShipperById(key);
        if (shipper == null) {
            return true;
        }
        CountDownLatch countDownLatch = new CountDownLatch(shipper.getThreadsNum());
        shipper.doDestroy(countDownLatch);
        removeShipper(shipper);
        try {
            if (countDownLatch.await(5, TimeUnit.SECONDS)) {
                LOGGER.debug("shipper退出完成");
            } else {
                LOGGER.warn("shipper退出超时，执行中断");
                shipper.forceStop();
            }
        } catch (InterruptedException e) {
            LOGGER.error(e);
        }

        return true;
    }


    @Override
    public void restart(BaseCacheManager cacheManager, Map<String, Object> stringObjectMap) {
//        worker.setMeter(MetricHelper.createMeter(MetricRegistry.name("shipper", shipperID, "eps")));

    }


}
