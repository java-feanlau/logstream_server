package com.boyitech.logstream.core.manager.indexer;

import com.boyitech.logstream.core.info.info.CountInfo;
import com.boyitech.logstream.core.info.info.SpeedInfo;
import com.boyitech.logstream.core.manager.BaseManager;
import com.boyitech.logstream.core.manager.cache.BaseCacheManager;
import com.boyitech.logstream.core.setting.StatsSettings;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.shipper.BaseShipper;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public abstract class BaseIndexerManager extends BaseManager implements IndexerManager {
	protected static Map<String, Class> indexMapping = new HashMap<>();

	private static final Map<String, BaseIndexer> indexerMap = new ConcurrentHashMap<>();
	private static final  Map<String , AtomicLong> countMap = new ConcurrentHashMap<>();
	private static final  Map<String , AtomicLong> failedcountMap = new ConcurrentHashMap<>();
	private static final Map<String , SpeedInfo> speedMap = new ConcurrentHashMap<>();
	private static final Map<String, CountInfo> bacthCountMap = new ConcurrentHashMap<>();

	public String getIndexType(String key) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		//根据id和索引查找相关的类获取type
		LOGGER.debug("获取索引类型， key: " + key);
		Class indexer = indexMapping.get(key);
		return indexer.getMethod("getType", null).invoke(null, null).toString();
	}


	// 修改为静态方法，供ESPorter调用
	public static Map getIndexMapping(String key) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		//根据id和索引查找相关的类获取mapping
		LOGGER.debug("获取Mapping， key: " + key);
		Class indexer = indexMapping.get(key);
		return (Map)indexer.getMethod("getMapping", null).invoke(null, null);
	}

	// 修改为静态方法，供ESPorter调用
	public static String getIndexMappingJson(String key) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		//根据id和索引查找相关的类获取mapping
		LOGGER.debug("获取Mapping， key: " + key);
		Class indexer = indexMapping.get(key);
		return (String)indexer.getMethod("getMappingString", null).invoke(null, null);
	}

	public Map<String, BaseIndexer> getIndexerMap(){
		return indexerMap;
	}

	public  Map<String, AtomicLong> getCountMap() {
		return countMap;
	}

	public Map<String, AtomicLong> getFailedcountMap() {
		return failedcountMap;
	}

	public abstract void restart(BaseCacheManager cacheManager, Map<String, Object> stringObjectMap);

	/*
	 * @Author Eric Zheng
	 * @Description 根据id获取indexer
	 * @Date 11:37 2019/6/27
	 **/
	public BaseIndexer getIndexerById(String indexerId){
		if(indexerId == null){
			return null;
		}
		return indexerMap.get(indexerId);

	}

	//for test
	public static void putMapping(String key,Class clazz){
		indexMapping.put(key, clazz);
	}


	public String getIndexerWorkerSpeed(String workerId) {
		if (workerId == null) {
			return "{}";
		}
		if (speedMap.get(workerId) == null | getIndexerById(workerId) == null) {
			return "{}";
		}
		SpeedInfo speedInfo = speedMap.get(workerId);
		String result = GsonHelper.toJson(speedInfo);
		return result;
	}


	public BaseIndexerManager() {
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


		for (String key : countMap.keySet()) {

			AtomicLong count = countMap.get(key);
			CountInfo countInfo = bacthCountMap.get(key);
			SpeedInfo speedInfo = speedMap.get(key);
			AtomicLong failedCount = failedcountMap.get(key);

			//防止并发删除的时候为空。
			if(count == null | countInfo == null | speedInfo == null || failedCount == null){
				continue;
			}

			countInfo.addCountAndTime(count.get());
			speedInfo.setAllSpeed(countInfo);

			//Graphite
			long now = System.currentTimeMillis() / 1000;
			String[] msg = new String[6];
			msg[0] = "yslogstream.worker.indexer." + key + ".mean_rate " + speedInfo.getSpeed() + " " + now;
			msg[1] = "yslogstream.worker.indexer." + key + ".m1_rate " + speedInfo.getOneMinuteSpeed() + " " + now;
			msg[2] = "yslogstream.worker.indexer." + key + ".m5_rate " + speedInfo.getFiveMinuteSpeed() + " " + now;
			msg[3] = "yslogstream.worker.indexer." + key + ".m15_rate " + speedInfo.getFifteenMinuteSpeed() + " " + now;
			msg[4] = "yslogstream.worker.indexer." + key + ".success_count " + count + " " + now;
			msg[5] = "yslogstream.worker.indexer." + key + ".fail_count " + failedCount.get() + " " + now;
			sendToGraphite(msg);
		}
	}

	protected void addIndexer(BaseIndexer indexer){
		String indexerID = indexer.getWorkerId();
		countMap.put(indexerID, indexer.getCountLong());
		failedcountMap.put(indexerID, indexer.getFailed());
		indexerMap.put(indexerID, indexer);
		speedMap.put(indexerID,new SpeedInfo());
		bacthCountMap.put(indexerID, new CountInfo(System.currentTimeMillis()));
	}


	protected void removeIndexer(BaseIndexer indexer){
		String indexerID = indexer.getWorkerId();
		countMap.remove(indexerID);
		failedcountMap.remove(indexerID);
		indexerMap.remove(indexerID);
		speedMap.remove(indexerID);
		bacthCountMap.remove(indexerID);
	}




}
