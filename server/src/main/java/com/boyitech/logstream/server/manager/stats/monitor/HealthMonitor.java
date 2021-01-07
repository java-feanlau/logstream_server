package com.boyitech.logstream.server.manager.stats.monitor;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.info.ClientShipperStatus;
import com.boyitech.logstream.core.info.ClientStatus;
import com.boyitech.logstream.core.manager.cache.BaseCacheManager;
import com.boyitech.logstream.core.manager.indexer.BaseIndexerManager;
import com.boyitech.logstream.core.manager.porter.BasePorterManager;
import com.boyitech.logstream.core.manager.shipper.BaseShipperManager;
import com.boyitech.logstream.core.setting.StatsSettings;
import com.boyitech.logstream.core.worker.BaseWorker;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.porter.BasePorter;
import com.boyitech.logstream.core.worker.shipper.BaseShipper;
import com.boyitech.logstream.server.manager.client.ServerClientManager;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author Eric
 * @Title: HealthMonitor
 * @date 2019/6/26 9:53
 * @Description: TODO
 */
public class HealthMonitor extends Thread {

    private BaseShipperManager shipperManager;
    private BaseIndexerManager indexerManager;
    private BasePorterManager porterManager;
    private BaseCacheManager cacheManger;
    private ServerClientManager clientManager;
    private Map<String, String> workerStatsMap;
    private Map<String, String> cacheStatsMap;
    private CountDownLatch countDownLatch;

    public HealthMonitor(BaseShipperManager shipperManager, BaseIndexerManager indexerManager,
                         BasePorterManager porterManager, BaseCacheManager CacheManger, ServerClientManager clientManager, Map<String, String> workerStatsMap, Map<String, String> cacheStatsMap, CountDownLatch countDownLatch) {
        this.shipperManager = shipperManager;
        this.indexerManager = indexerManager;
        this.porterManager = porterManager;
        this.cacheManger = CacheManger;
        this.clientManager = clientManager;
        this.workerStatsMap = workerStatsMap;
        this.cacheStatsMap = cacheStatsMap;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        Map<String, BaseShipper> shipperMap = shipperManager.getShipperMap();
        Map<String, BaseIndexer> indexerMap = indexerManager.getIndexerMap();
        Map<String, BasePorter> porterMap = porterManager.getPorterMap();
        Map<String, ClientStatus> registeredClients = clientManager.getRegisteredClients();
        Map<String, BaseCache> lv1CacheMap = cacheManger.getLv1CacheMap();
        Map<String, BaseCache> lv2CacheMap = cacheManger.getLv2CacheMap();
        Map<String, BaseCache> lv3CacheMap = cacheManger.getLv3CacheMap();
        for (; ; ) {
            try {
                //遍历所有Worker查询状态并记录
                workerStatsMap.clear();
                for (BaseShipper shipper : shipperMap.values()) {
                    checkWorkerStatus(shipper);
                }
                for (BaseIndexer indexer : indexerMap.values()) {
                    checkWorkerStatus(indexer);
                }
                for (BasePorter porter : porterMap.values()) {
                    checkWorkerStatus(porter);
                }


                for (ClientStatus clientStatus : registeredClients.values()) {
                    Map<String, ClientShipperStatus> clientShipperStatusMap = clientStatus.getClientShipperStatusMap();
                    for (Map.Entry<String, ClientShipperStatus> entry : clientShipperStatusMap.entrySet()) {
                        workerStatsMap.put(entry.getKey(), entry.getValue().isRunning() ? "200" : "400");
                    }
                }


                //遍历所有Cache查询状态并记录
                cacheStatsMap.clear();
                for (BaseCache cache : lv1CacheMap.values()) {
                    cacheStatsMap.put(cache.getCacheId(), cache.size() + "");
                }
                for (BaseCache cache : lv2CacheMap.values()) {
                    cacheStatsMap.put(cache.getCacheId(), cache.size() + "");
                }
                for (BaseCache cache : lv3CacheMap.values()) {
                    cacheStatsMap.put(cache.getCacheId(), cache.size() + "");
                }


            } catch (NullPointerException e) {
                e.printStackTrace();
            } finally {
                if (countDownLatch.getCount() == 1) {
                    countDownLatch.countDown();
                }
                try {
                    Thread.sleep(Integer.parseInt(StatsSettings.STATS_QUERY_SLEEP_TIME.getValue()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }
    }

    //正在运行 200，暂停状态 400，销毁返回null
    public void checkWorkerStatus(BaseWorker worker) {
        if (worker.isAlive()) {
            workerStatsMap.put(worker.getWorkerId(), "200");
        } else {
            workerStatsMap.put(worker.getWorkerId(), "400");
        }
    }
}
