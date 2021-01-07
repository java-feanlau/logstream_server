package com.boyitech.logstream.core.factory;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.setting.CacheSettings;
import com.boyitech.logstream.core.cache.memory.MemoryCache;

public class CacheFactory {



	/**
	 * 创建默认的内存缓存
	 *
	 * @return
	 */
	public static BaseCache createCache() {
		BaseCache cache = new MemoryCache(CacheSettings.MEMORYCACHESIZE.getValue());
		return cache;
	}

	public static BaseCache createCache(String cacheID,Integer size) {
		BaseCache cache = new MemoryCache(cacheID,size);
		return cache;
	}

	/**
	 * 创建默认的全局内存缓存
	 *
	 * @return
	 */
	public static BaseCache createGlobalCache() {
		BaseCache cache = new MemoryCache(CacheSettings.GLOBALCACHESIZE.getValue());
		return cache;
	}

}
