package com.boyitech.logstream.core.manager.porter;

import com.boyitech.logstream.core.info.info.CountInfo;
import com.boyitech.logstream.core.info.info.SpeedInfo;
import com.boyitech.logstream.core.manager.BaseManager;
import com.boyitech.logstream.core.manager.cache.BaseCacheManager;
import com.boyitech.logstream.core.setting.StatsSettings;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.porter.BasePorter;
import com.boyitech.logstream.core.worker.porter.elasticsearch.ElasticsearchPorter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public abstract class BasePorterManager extends BaseManager implements PorterManager {

    private static final Map<String, BasePorter> porterMap = new ConcurrentHashMap();
    private static final Map<String, AtomicLong> countMap = new ConcurrentHashMap();
    private static final Map<String , SpeedInfo> speedMap = new ConcurrentHashMap<>();
    private static final Map<String, CountInfo> bacthCountMap = new ConcurrentHashMap<>();

    public Map<String, BasePorter> getPorterMap() {
        return porterMap;
    }

    public abstract void restart(BaseCacheManager cacheManager, Map<String, Object> stringObjectMap);

    public Map<String, AtomicLong> getCountMap() {
        return countMap;
    }

    /*
     * @Author Eric Zheng
     * @Description 根据id获取porter
     * @Date 11:43 2019/6/27
     **/
    public BasePorter getPorterById(String porterId){
        if (porterId == null){
            return null;
        }
        return porterMap.get(porterId);
    }


    public String getPorterWorkerSpeed(String workerId) {
        if (workerId == null) {
            return "{}";
        }
        if (speedMap.get(workerId) == null | getPorterById(workerId) == null) {
            return "{}";
        }
        SpeedInfo speedInfo = speedMap.get(workerId);
        String result = GsonHelper.toJson(speedInfo);
        return result;
    }

    public BasePorterManager() {
        ExecutorService executorServer = Executors.newSingleThreadExecutor();
        executorServer.submit(()->{
            while (true){
                conutMonitor();
                Thread.sleep(Integer.parseInt(StatsSettings.STATS_COUNT_SLEEP_TIME.getValue()));
            }
        });

    }

    /*
     * @Author Eric Zheng
     * @Description 记录解析速率
     * @Date 16:29 2019/8/8
     **/
    private void conutMonitor(){
        String[] msg = new String[5];
        String[] only = new String[5];

        for (String key : countMap.keySet()) {

            AtomicLong count = countMap.get(key);
            CountInfo countInfo = bacthCountMap.get(key);
            SpeedInfo speedInfo = speedMap.get(key);
            BasePorter porter = getPorterById(key);
            //防止并发删除的时候为空。
            if(count == null | countInfo == null | speedInfo == null | porter == null){
                continue;
            }

            countInfo.addCountAndTime(count.get());
            speedInfo.setAllSpeed(countInfo);

            //Graphite
            long now = System.currentTimeMillis() / 1000;
            msg[0] = "yslogstream.worker.porter." + key + ".mean_rate " + speedInfo.getSpeed() + " " + now;
            msg[1] = "yslogstream.worker.porter." + key + ".m1_rate " + speedInfo.getOneMinuteSpeed() + " " + now;
            msg[2] = "yslogstream.worker.porter." + key + ".m5_rate " + speedInfo.getFiveMinuteSpeed() + " " + now;
            msg[3] = "yslogstream.worker.porter." + key + ".m15_rate " + speedInfo.getFifteenMinuteSpeed() + " " + now;
            msg[4] = "yslogstream.worker.porter." + key + ".count " + count + " " + now;
            sendToGraphite(msg);


            if (porter != null) {
                if (porter instanceof ElasticsearchPorter) {
                    only[0] = "yslogstream.worker.porter.total.mean_rate " + speedInfo.getSpeed() + " " + now;
                    only[1] = "yslogstream.worker.porter.total.m1_rate " + speedInfo.getOneMinuteSpeed() + " " + now;
                    only[2] = "yslogstream.worker.porter.total.m5_rate " + speedInfo.getFiveMinuteSpeed() + " " + now;
                    only[3] = "yslogstream.worker.porter.total.m15_rate " + speedInfo.getFifteenMinuteSpeed() + " " + now;
                    only[4] = "yslogstream.worker.porter.total.count " + count + " " + now;
                    sendToGraphite(only);
                }
            }
            sendToGraphite(msg);
        }
    }

    protected void addPorter(BasePorter porter){
        String porterID = porter.getWorkerId();
        countMap.put(porterID, porter.getCount());
        porterMap.put(porterID, porter);
        speedMap.put(porterID,new SpeedInfo());
        bacthCountMap.put(porterID, new CountInfo(System.currentTimeMillis()));
    }


    protected void removePorter(BasePorter porter){
        String porterID = porter.getWorkerId();
        countMap.remove(porterID);
        porterMap.remove(porterID);
        speedMap.remove(porterID);
        bacthCountMap.remove(porterID);
    }

}

