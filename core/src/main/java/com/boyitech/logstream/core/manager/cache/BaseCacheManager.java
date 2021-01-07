package com.boyitech.logstream.core.manager.cache;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.manager.BaseManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseCacheManager extends BaseManager implements CacheManager {

	protected final Map<String, BaseCache> lv1CacheMap = new ConcurrentHashMap();
	protected final Map<String, BaseCache> lv2CacheMap = new ConcurrentHashMap();
	protected final Map<String, BaseCache> lv3CacheMap = new ConcurrentHashMap();
	protected final Map<String, BaseCache> globalCacheMap = new ConcurrentHashMap();

	public Map<String, BaseCache> getLv1CacheMap(){
		return lv1CacheMap;
	}
	public Map<String, BaseCache> getLv2CacheMap(){
		return lv2CacheMap;
	}
	public Map<String, BaseCache> getLv3CacheMap(){
		return lv3CacheMap;
	}

	public abstract  void restart(Map<String, Object> stringObjectMap);

	public abstract boolean destoryCache(String cacheID);
}
