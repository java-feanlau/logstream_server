package com.boyitech.logstream.server.manager.indexer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.factory.WorkerFactory;
import com.boyitech.logstream.core.info.info.CountInfo;
import com.boyitech.logstream.core.info.info.SpeedInfo;
import com.boyitech.logstream.core.manager.cache.BaseCacheManager;
import com.boyitech.logstream.core.manager.indexer.BaseIndexerManager;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.IPv4Util;
import com.boyitech.logstream.core.util.jdbc.DBUtil;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import com.boyitech.logstream.core.worker.shipper.BaseShipper;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class YSIndexerManager extends BaseIndexerManager {


    @Override
    public BaseIndexer createIndexer(String confg, BaseCache lv1cache, BaseCache lv2cache) {


        Map configMap = GsonHelper.fromJson(confg);
        BaseIndexerConfig indexerConfig = new BaseIndexerConfig(configMap);
        BaseIndexer worker = null;
        String indexerID = null;

        String ipFilter=indexerConfig.getIpFilter();
        if(GrokUtil.isStringHasValue(ipFilter)){
            Map mapIpFilter = JSONObject.parseObject(ipFilter);
            for (Object key : mapIpFilter.keySet()) {
                JSONArray ipValues = (JSONArray) mapIpFilter.get(key);
                if(ipValues.size()>0){
                    for(int i=0;i<ipValues.size();i++){
                        if(!IPv4Util.isIPv4s(ipValues.get(i).toString())){
                            return null;
                        }
                    }
                }
            }
        }

        try {
            worker = WorkerFactory.createIndexerWorker(indexerConfig);
            indexerID = worker.getWorkerId();
        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            LOGGER.error("以配置 " + confg + " 创建indexer失败", e);
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("indexer_id", indexerID);
        map.put("indexer_config", confg);
        map.put("lv1cache", lv1cache.getCacheId());
        map.put("lv2cache", lv2cache.getCacheId());
        //以下插入状态：run_status初始化0表示停止状态，1代表运行状态，
        map.put("run_status", "0");
        try {
            DBUtil.insert("ys_worker_indexer", map);
        } catch (SQLException e) {
            LOGGER.error("Indexer:" + indexerID + "持久化失败: " + e.getMessage());
        }
        LOGGER.info("Indexer:" + indexerID + ",创建成功");


        worker.setLv1Cache(lv1cache);
        worker.setLv2Cache(lv2cache);
        addIndexer(worker);

        indexMapping.put(indexerConfig.getLogType(), worker.getClass());
        //获取状态添加到list
        LOGGER.debug("添加" + indexerConfig.getLogType() + "类对象的indexMapping到缓存");
        return worker;
    }


    @Override
    public boolean startWorker(String key) {
        BaseIndexer indexer = getIndexerById(key);
        if (indexer == null)
            return false;
        if (!indexer.doStart()) {
            return false;
        }
        changeRunStatus("1", key, this.getClass().getName());
        return true;
    }

    @Override
    public boolean stopWorker(String key) {
        BaseIndexer indexer = getIndexerById(key);
        if (indexer == null) {
            return false;
        }
        changeRunStatus("0", key, this.getClass().getName());
        CountDownLatch countDownLatch = new CountDownLatch(indexer.getThreadsNum());
        indexer.doStop(countDownLatch);
        try {
            if (countDownLatch.await(5, TimeUnit.SECONDS)) {
                LOGGER.debug("indexer退出完成");
            } else {
                LOGGER.warn("indexer退出超时，执行中断");
                indexer.forceStop();
            }
        } catch (InterruptedException e) {
            LOGGER.error(e);
            return false;
        }

        return true;
    }


    @Override
    public boolean destroyWorker(String key) {
        BaseIndexer indexer = getIndexerById(key);
        //高：删除的时候如果如果不存该worker，希望返回的是true。
        if (indexer == null) {
            return true;
        }
        CountDownLatch countDownLatch = new CountDownLatch(indexer.getThreadsNum());
        indexer.doDestroy(countDownLatch);
        HashMap<String, Object> delMap = new HashMap<>();
        delMap.put("indexer_id", key);
        changeRunStatus("0", key, this.getClass().getName());
        try {
            DBUtil.delete("ys_worker_indexer", delMap);
        } catch (SQLException e) {
            LOGGER.error("IndexerWorker:" + key + "从数据库删除失败");
        }
        removeIndexer(indexer);
        return true;

    }


    @Override
    public void restart(BaseCacheManager cacheManager, Map<String, Object> stringObjectMap) {
        if (stringObjectMap == null) {
            return;
        }
        String indexer_id = (String) stringObjectMap.get("indexer_id");
        String indexer_config = (String) stringObjectMap.get("indexer_config");
        String lv1_cache = (String) stringObjectMap.get("lv1cache");
        String lv2_cache = (String) stringObjectMap.get("lv2cache");
        String run_status = (String) stringObjectMap.get("run_status");
        BaseCache lv1Cache = cacheManager.getLv1Cache(lv1_cache);
        BaseCache lv2Cache = cacheManager.getLv2Cache(lv2_cache);

        Map configMap = GsonHelper.fromJson(indexer_config);
        BaseIndexerConfig indexerConfig = new BaseIndexerConfig(configMap);
        BaseIndexer worker = null;
        try {
            worker = WorkerFactory.createIndexerWorker(indexer_id, indexerConfig);


            worker.setLv1Cache(lv1Cache);
            worker.setLv2Cache(lv2Cache);

            addIndexer(worker);
            indexMapping.put(indexerConfig.getLogType(), worker.getClass());
            //查询重启之前shipper的状态,并正确重启采集任务
            if (run_status.equals("1"))
                worker.doStart();
            //获取状态添加到list
            LOGGER.debug("添加" + indexerConfig.getLogType() + "类对象的indexMapping到缓存");
        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            LOGGER.error("恢复 " + indexer_id + " 失败", e);
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
        whereMap.put("indexer_id", key);
        try {
            DBUtil.update("ys_worker_indexer", valueMap, whereMap);
        } catch (SQLException e) {
            LOGGER.error("在" + className + "中更新状态失败！" + e.getMessage());
            return false;
        }
        return true;
    }



}
