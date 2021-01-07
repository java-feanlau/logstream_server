package com.boyitech.logstream.server.manager.stats;

import com.boyitech.logstream.core.manager.BaseManager;
import com.boyitech.logstream.core.manager.cache.BaseCacheManager;
import com.boyitech.logstream.core.manager.indexer.BaseIndexerManager;
import com.boyitech.logstream.core.manager.porter.BasePorterManager;
import com.boyitech.logstream.core.manager.shipper.BaseShipperManager;
import com.boyitech.logstream.server.manager.client.ServerClientManager;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.server.manager.stats.monitor.HealthMonitor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author Eric
 * @Title: BaseStatsManager
 * @date 2019/3/18 11:40
 * @Description: TODO
 */
public class StatsManager extends BaseManager {

    private BaseShipperManager shipperManager;
    private BaseIndexerManager indexerManager;
    private BasePorterManager porterManager;
    private BaseCacheManager cacheManger;
    private ServerClientManager clientManager;

    //记录工作woreker状态<WorkerId,status> （workerID,200）
    private static final Map<String, String> workerStatsMap = new ConcurrentHashMap<>();
    private static final Map<String, String> cacheStatsMap = new ConcurrentHashMap<>();



    public StatsManager(BaseShipperManager shipperManager, BaseIndexerManager indexerManager,
                        BasePorterManager porterManager, BaseCacheManager CacheManger, ServerClientManager clientManager) {
        this.shipperManager = shipperManager;
        this.indexerManager = indexerManager;
        this.porterManager = porterManager;
        this.cacheManger = CacheManger;
        this.clientManager = clientManager;
        startMonitor();

    }

    /*
     * @Author Eric Zheng
     * @Description 该方法是优化状态监听的延迟，可以通过手动的方式来更新worker的状态码，而非被动等待
     * @Date 16:04 2019/6/25
     **/
    public void changeWorkerStatus(String workerID, String status) {
        if (workerID != null) {
            workerStatsMap.put(workerID, status);
        }

    }

    /*
     * @Author Eric Zheng
     * @Description 监控worker与cache的状态
     * @Date 15:59 2019/6/25
     **/
    public void startMonitor() {
        CountDownLatch healthCountDownLatch = new CountDownLatch(1);
        new HealthMonitor(shipperManager, indexerManager,
                porterManager, cacheManger, clientManager, workerStatsMap, cacheStatsMap, healthCountDownLatch).start();
        try {
            healthCountDownLatch.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /*
     * @Author Eric Zheng
     * @Description 查询所有Worker状态信息
     * {"89039c08-83fe-49be-b240-3d897362fba3":"200","9f456465-5ef2-424c-8cd6-15df609df02c":"200"}
     * @Date 16:12 2019/3/18
     **/
    public String getAllWorkersStats() {
        String result = GsonHelper.toJson(workerStatsMap);
        return result;
    }

    /**
     * @Author Eric Zheng
     * @Description 根据id查询worker信息
     * @Date 16:13 2019/3/18
     **/
    //todo tcpShipper无法记录
    public String getWorkerStatsByKey(String workerID) {
        String workerStats = workerStatsMap.get(workerID);
        return workerStats;
    }


    public String getAllCachesStats() {

        String result = GsonHelper.toJson(cacheStatsMap);

        return result;
    }

    public String getCacheStatsByKey(String cacheID) {
        if(cacheID == null){
            return "{}";
        }
        String result = cacheStatsMap.get(cacheID);
        if(result == null){
            return "{}";
        }
        return result;
    }





    public void clearShipperWorkerById(String workerId) {
        if (workerId != null && workerStatsMap.get(workerId) != null) {
            workerStatsMap.remove(workerId);
        }
    }

    public void clearIndexerWorkerById(String workerId) {
        if (workerId != null && workerStatsMap.get(workerId) != null) {
            workerStatsMap.remove(workerId);
        }

    }

    public void clearPorterWorkerById(String workerId) {
        if (workerId != null && workerStatsMap.get(workerId) != null) {
            workerStatsMap.remove(workerId);
        }
    }


}
