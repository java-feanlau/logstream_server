package com.boyitech.logstream.server.manager.shipper;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.factory.ConfigFactory;
import com.boyitech.logstream.core.factory.WorkerFactory;
import com.boyitech.logstream.core.manager.cache.BaseCacheManager;
import com.boyitech.logstream.core.manager.shipper.BaseShipperManager;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.jdbc.DBUtil;
import com.boyitech.logstream.core.worker.shipper.BaseShipper;
import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class YSShipperManager extends BaseShipperManager {

    @Override
    public BaseShipper createShipper(String shipperConfig, BaseCache lv1Cache) {
        if(lv1Cache == null){
            return null;
        }
        //通过工厂类创建Worker实例
        //使用工厂类，创建出一个具体的最子类的ShipperConfig对象并且用BaseShipperConifg来引用
        Map configMap = GsonHelper.fromJson(shipperConfig);
        BaseShipperConfig baseShipperConfig = null;
        try {
            baseShipperConfig = ConfigFactory.buildShipperConfig(configMap);
        } catch (Exception e) {
            LOGGER.error("配置文件有误，创建shipper失败:"+e);
            return null;
        }
        BaseShipper worker = null;
        String shipperID = null;
        try {
            worker = WorkerFactory.createShipperWorker(baseShipperConfig);
            shipperID = worker.getWorkerId();
        } catch (Exception e) {
            LOGGER.error("ShipperWorker:" + shipperID + "创建失败:"+e);
            return null;
        }



        worker.setLv1Cache(lv1Cache);
        worker.setMark(shipperID);
        addShipper(worker);
        LOGGER.info("ShipperWorker:" + shipperID + "持久化并且创建完成");

        try {
            Map<String, Object> map = new HashMap<>();
            map.put("shipper_id", shipperID);
            map.put("shipper_config", shipperConfig);
            map.put("lv1cache", lv1Cache.getCacheId());
            //以下插入状态：run_status初始化0表示停止状态，1代表运行状态，
            map.put("run_status", "0");
            DBUtil.insert("ys_worker_shipper", map);
        } catch (SQLException e) {
            LOGGER.error("ShipperWorker:" + shipperID + "持久化失败: " + e.getMessage());
        }
        return worker;
    }

    @Override
    public BaseShipper createShipper(String shipperID, String shipperConfig, BaseCache lv1cache) {
        return null;
    }


    @Override
    public boolean startWorker(String key) {
        BaseShipper shipper = getShipperById(key);
        if (shipper == null)
            return false;
        if (!shipper.doStart()) {
            return false;
        }
        changeRunStatus("1",key,this.getClass().getName());
        return true;
    }

    @Override
    public synchronized boolean stopWorker(String key) {

        BaseShipper shipper = getShipperById(key);

        if (shipper == null) {
            return false;
        }else if(!shipper.isAlive()){
            return true;
        }
        CountDownLatch countDownLatch = new CountDownLatch(shipper.getThreadsNum());
        shipper.doStop(countDownLatch);
        changeRunStatus("0",key,this.getClass().getName());
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
    public synchronized boolean destroyWorker(String key) {
        //高：删除的时候如果如果不存该worker，希望返回的是true。
        BaseShipper shipper = getShipperById(key);
        if (shipper == null) {
            return true;
        }
        CountDownLatch countDownLatch = new CountDownLatch(shipper.getThreadsNum());
        shipper.doDestroy(countDownLatch);

        try {
            HashMap<String, Object> delMap = new HashMap<>();
            delMap.put("shipper_id", key);
            changeRunStatus("0",key,this.getClass().getName());
            DBUtil.delete("ys_worker_shipper", delMap);
        } catch (SQLException e) {
            LOGGER.error("ShipperWorker:" + key + "从数据库删除失败");
        }
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
        if (stringObjectMap == null) {
            return;
        }
        String shipper_id = (String) stringObjectMap.get("shipper_id");
        String shipper_config = (String) stringObjectMap.get("shipper_config");
        String lv1_cache = (String) stringObjectMap.get("lv1cache");
        String run_status=(String)stringObjectMap.get("run_status") ;
        BaseCache lv1Cache = cacheManager.getLv1Cache(lv1_cache);

        Map configMap = GsonHelper.fromJson(shipper_config);
        BaseShipperConfig baseShipperConfig = null;
        try {
            baseShipperConfig = ConfigFactory.buildShipperConfig(configMap);
        } catch (Exception e) {
            LOGGER.error("以配置 " + shipper_config + " 创建shipper失败", e);
            return;
        }
        BaseShipper worker = null;
        try {
            worker = WorkerFactory.createShipperWorker(shipper_id, baseShipperConfig);
            worker.setLv1Cache(lv1Cache);
            addShipper(worker);

            //查询重启之前shipper的状态,并正确重启采集任务
            if(run_status.equals("1"))
                worker.doStart();
        } catch (Exception e) {
            LOGGER.error("ShipperWorker:" + shipper_id + "恢复失败");
        }

//        worker.setMeter(MetricHelper.createMeter(MetricRegistry.name("shipper", shipperID, "eps")));

    }

    /*
     * @Author juzheng
     * @Time 20190626
     * @Description 在worker的运行状态发生改变时更新;
     **/
    public boolean changeRunStatus(String runStatus, String key, String className) {
        //以下更改启动状态
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("run_status", runStatus);
        Map<String, Object> whereMap = new HashMap<>();
        whereMap.put("shipper_id", key);
        try {
            DBUtil.update("ys_worker_shipper", valueMap, whereMap);
        } catch (SQLException e) {
            LOGGER.error("在" + className + "中更新状态失败！" + e.getMessage());
            return false;
        }
        return true;
    }

}
