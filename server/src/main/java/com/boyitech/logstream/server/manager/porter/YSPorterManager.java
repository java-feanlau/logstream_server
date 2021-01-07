package com.boyitech.logstream.server.manager.porter;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.factory.ConfigFactory;
import com.boyitech.logstream.core.factory.WorkerFactory;
import com.boyitech.logstream.core.manager.cache.BaseCacheManager;
import com.boyitech.logstream.core.manager.porter.BasePorterManager;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.jdbc.DBUtil;
import com.boyitech.logstream.core.worker.porter.BasePorter;
import com.boyitech.logstream.core.worker.porter.BasePorterConfig;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class YSPorterManager extends BasePorterManager {


    @Override
    public BasePorter createPorter(String config, BaseCache lv2cache, BaseCache lv3cache) {

        Map configMap = GsonHelper.fromJson(config);
        BasePorterConfig basePorterConfig = ConfigFactory.buildPorterConfig(configMap);

        BasePorter porter = null;
        String id = null;
        try {
            porter = WorkerFactory.createPorterWorker(basePorterConfig);
            id = porter.getWorkerId();
        } catch (Exception e) {
            LOGGER.error("PorterWorker:" + id + "创建失败" + e.getMessage());
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("porter_id", id);
        map.put("porter_config", config);
        //以下插入状态：run_status初始化0表示停止状态，1代表运行状态，
        map.put("run_status", "0");
        if (lv2cache == null) {
            map.put("lv2cache", null);
        } else {
            map.put("lv2cache", lv2cache.getCacheId());
        }
        if (lv3cache == null) {
            map.put("lv3cache", null);
        } else {
            map.put("lv3cache", lv3cache.getCacheId());
        }
        try {
            DBUtil.insert("ys_worker_porter", map);
        } catch (SQLException e) {
            LOGGER.error("PorterWorker:" + id + "持久化失败 " + e.getMessage());
        }


        porter.setLv2Cache(lv2cache);
        porter.setLv3Cache(lv3cache);
        addPorter(porter);
        return porter;
    }


    @Override
    public boolean startWorker(String key) {
        BasePorter porter = getPorterById(key);
        if (porter == null)
            return false;
        if (!porter.doStart()) {
            return false;
        }
        changeRunStatus("1", key, this.getClass().getName());
        return true;
    }

    @Override
    public boolean stopWorker(String key) {
        BasePorter porter = getPorterById(key);
        if (porter == null) {
            return false;
        }
        changeRunStatus("0", key, this.getClass().getName());
        CountDownLatch countDownLatch = new CountDownLatch(porter.getThreadsNum());
        porter.doStop(countDownLatch);
        try {
            if (countDownLatch.await(5, TimeUnit.SECONDS)) {
                LOGGER.debug("shipper退出完成");
            } else {
                LOGGER.warn("shipper退出超时，执行中断");
                porter.forceStop();
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
        BasePorter porter = getPorterById(key);
        if (porter == null) {
            return true;
        }
        CountDownLatch countDownLatch = new CountDownLatch(porter.getThreadsNum());
        porter.doDestroy(countDownLatch);
        HashMap<String, Object> delMap = new HashMap<>();
        delMap.put("porter_id", key);
        changeRunStatus("0", key, this.getClass().getName());
        try {
            DBUtil.delete("ys_worker_porter", delMap);
        } catch (SQLException e) {
            LOGGER.error("PorterWorker:" + key + "从数据库删除失败");
        }
        removePorter(porter);
        try {
            if (countDownLatch.await(5, TimeUnit.SECONDS)) {
                LOGGER.debug("PorterWorker");
            } else {
                LOGGER.warn("PorterWorker退出超时，执行中断");
                porter.forceStop();
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
        String porter_id = (String) stringObjectMap.get("porter_id");
        String porter_config = (String) stringObjectMap.get("porter_config");
        String lv2_cache = (String) stringObjectMap.get("lv2cache");
        String lv3_cache = (String) stringObjectMap.get("lv3cache");
        String run_status = (String) stringObjectMap.get("run_status");
        BaseCache lv2cache = null;
        BaseCache lv3cache = null;
        if (lv2_cache != null) {
            lv2cache = cacheManager.getLv2CacheMap().get(lv2_cache);
        }
        if (lv3_cache != null) {
            lv3cache = cacheManager.getLv3CacheMap().get(lv3_cache);
        }

        Map configMap = GsonHelper.fromJson(porter_config);
        BasePorterConfig basePorterConfig = ConfigFactory.buildPorterConfig(configMap);

        BasePorter porter;

        try {
            porter = WorkerFactory.createPorterWorker(porter_id, basePorterConfig);
            porter.setLv2Cache(lv2cache);
            porter.setLv3Cache(lv3cache);

            addPorter(porter);

            //查询重启之前shipper的状态,并正确重启采集任务
            if (run_status.equals("1"))
                porter.doStart();
        } catch (Exception e) {
            LOGGER.error("PorterWorker:" + porter_id + "恢复失败" + e.getMessage());
        }
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
        whereMap.put("porter_id", key);
        try {
            DBUtil.update("ys_worker_porter", valueMap, whereMap);
        } catch (SQLException e) {
            LOGGER.error("在" + className + "中更新状态失败！" + e.getMessage());
            return false;
        }
        return true;
    }
}
