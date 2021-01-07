package com.boyitech.logstream.core.manager.shipper;

import com.boyitech.logstream.core.info.info.CountInfo;
import com.boyitech.logstream.core.info.info.SpeedInfo;
import com.boyitech.logstream.core.manager.BaseManager;
import com.boyitech.logstream.core.manager.cache.BaseCacheManager;
import com.boyitech.logstream.core.setting.StatsSettings;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.shipper.BaseShipper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public abstract class BaseShipperManager extends BaseManager implements ShipperManager {

	private static final Map<String, BaseShipper> shipperMap = new ConcurrentHashMap<>();
	private static final Map<String, AtomicLong> countMap = new ConcurrentHashMap<>();
	private static final Map<String, SpeedInfo> speedMap = new ConcurrentHashMap<>();
	private static final Map<String, CountInfo> bacthCountMap = new ConcurrentHashMap<>();

	public Map<String, BaseShipper> getShipperMap() {
		return shipperMap;
	}

	public Map<String, AtomicLong> getCountMap() {
		return countMap;
	}

	public abstract void restart(BaseCacheManager cacheManager, Map<String, Object> stringObjectMap);

	public BaseShipper getShipperById(String shipperID) {
		if (shipperID == null) {
			return null;
		}
		return shipperMap.get(shipperID);
	}

	public String getShipperWorkerSpeed(String workerId) {
		if (workerId == null) {
			return "{}";
		}
		if (speedMap.get(workerId) == null | getShipperById(workerId) == null) {
			return "{}";
		}
		SpeedInfo speedInfo = speedMap.get(workerId);
		String result = GsonHelper.toJson(speedInfo);
		return result;
	}

	public BaseShipperManager() {
		ExecutorService executorServer = Executors.newSingleThreadExecutor();
		executorServer.submit(() -> {
			while (true) {
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
	private void conutMonitor() {
		for (String key : countMap.keySet()) {

			AtomicLong count = countMap.get(key);
			CountInfo countInfo = bacthCountMap.get(key);
			SpeedInfo speedInfo = speedMap.get(key);

			//防止并发删除的时候为空。
			if(count == null | countInfo == null | speedInfo == null){
				continue;
			}

			countInfo.addCountAndTime(count.get());
			speedInfo.setAllSpeed(countInfo);

			//Graphite
			long now = System.currentTimeMillis() / 1000;
			String[] msg = new String[5];
			msg[0] = "yslogstream.worker.shipper." + key + ".mean_rate " + speedInfo.getSpeed() + " " + now;
			msg[1] = "yslogstream.worker.shipper." + key + ".m1_rate " + speedInfo.getOneMinuteSpeed() + " " + now;
			msg[2] = "yslogstream.worker.shipper." + key + ".m5_rate " + speedInfo.getFiveMinuteSpeed() + " " + now;
			msg[3] = "yslogstream.worker.shipper." + key + ".m15_rate " + speedInfo.getFifteenMinuteSpeed() + " " + now;
			msg[4] = "yslogstream.worker.shipper." + key + ".count " + count + " " + now;
			sendToGraphite(msg);
		}
	}

	protected void addShipper(BaseShipper shipper) {
		String shipperID = shipper.getWorkerId();
		countMap.put(shipperID, shipper.getCount());
		shipperMap.put(shipperID, shipper);
		speedMap.put(shipperID, new SpeedInfo());
		bacthCountMap.put(shipperID, new CountInfo(System.currentTimeMillis()));
	}


	protected void removeShipper(BaseShipper shipper) {
		String shipperID = shipper.getWorkerId();
		countMap.remove(shipperID);
		shipperMap.remove(shipperID);
		speedMap.remove(shipperID);
		bacthCountMap.remove(shipperID);
	}



}
