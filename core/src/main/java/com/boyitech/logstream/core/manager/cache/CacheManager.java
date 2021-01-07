package com.boyitech.logstream.core.manager.cache;

import com.boyitech.logstream.core.cache.BaseCache;

public interface CacheManager {

	/**
	 * 根据key创建对应的lv1cache,如果已经存在则覆盖
	 * @param logStreamId
	 * @return
	 */
	public abstract String createLv1Cache();

	/**
	 * 根据key获取对应的lv1cache,如果不存在则创建新的,如果存在则返回已有的
	 * @param logStreamId
	 * @return
	 */
	public abstract BaseCache getLv1Cache(String key);

	/**
	 * 根据key创建对应的lv2cache,如果已经存在则覆盖
	 * @param logStreamId
	 * @return
	 */
	public abstract String createLv2Cache();

	/**
	 * 根据key获取对应的lv2cache,如果不存在则创建新的,如果存在则返回已有的
	 * @param logStreamId
	 * @return
	 */
	public abstract BaseCache getLv2Cache(String key);

	/**
	 * 根据key创建对应的lv2cache,如果已经存在则覆盖
	 * @param logStreamId
	 * @return
	 */
	public abstract String createLv3Cache();

	/**
	 * 根据key获取对应的lv2cache,如果不存在则创建新的,如果存在则返回已有的
	 * @param logStreamId
	 * @return
	 */
	public abstract BaseCache getLv3Cache(String key);

	/**
	 * 获取格式化类型对应的全局缓存，如果不存在则创建
	 *
	 * @param logStreamId
	 * @return
	 */
	public abstract BaseCache getGlobalCacheByType(String key);

}
