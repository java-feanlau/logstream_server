package com.boyitech.logstream.client.manager.cache;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.factory.CacheFactory;
import com.boyitech.logstream.core.manager.cache.BaseCacheManager;

import java.util.Map;


public class YSClientCacheManager extends BaseCacheManager {

    //创建默认的内存缓存
//	private final BaseCache lv2Cache = CacheFactory.createCache();
//	private final BaseCache lv3Cache = CacheFactory.createCache();

    @Override
    public String createLv1Cache() {
        String cacheID = null;

        BaseCache cache = CacheFactory.createCache();
        cacheID = cache.getCacheId();
        lv1CacheMap.put(cacheID, cache);
        LOGGER.info("Cache:" + cacheID + ",创建成功");

        return cacheID;
    }

    @Override
    public BaseCache getLv1Cache(String cacheID) {
        if (cacheID == null) {
            return null;
        }
        return lv1CacheMap.get(cacheID);
    }

    @Override
    public String createLv2Cache() {
        String cacheID = null;

        BaseCache cache = CacheFactory.createCache();
        cacheID = cache.getCacheId();
        lv2CacheMap.put(cacheID, cache);
        LOGGER.info("Cache:" + cacheID + ",创建成功");

        return cacheID;
    }

    @Override
    public BaseCache getLv2Cache(String cacheID) {
        if (cacheID == null) {
            return null;
        }
        return lv2CacheMap.get(cacheID);
    }

    @Override
    public String createLv3Cache() {

        BaseCache cache = CacheFactory.createCache();
        String cacheID = cache.getCacheId();
        lv3CacheMap.put(cacheID, cache);
        LOGGER.info("Cache:" + cacheID + ",创建成功");

        return cacheID;
    }

    @Override
    public BaseCache getLv3Cache(String cacheID) {
        if (cacheID == null) {
            return null;
        }
        return lv3CacheMap.get(cacheID);
    }


    @Override
    public BaseCache getGlobalCacheByType(String cacheID) {
        return null;
    }

    /*
     * @Author Eric Zheng
     * @Description 获取各个manager缓存的cache，返回给statsmanager维护一份相同的
     * @Date 11:46 2019/3/18
     **/
    public Map<String, BaseCache> getAllBaseCachelv1() {
        return lv1CacheMap;
    }

    public Map<String, BaseCache> getAllBaseCachelv2() {
        return lv2CacheMap;
    }

    public Map<String, BaseCache> getAllBaseCachelv3() {
        return lv3CacheMap;
    }

    @Override
    public void restart(Map<String, Object> stringObjectMap) {
    }

    @Override
    public boolean destoryCache(String cacheID) {
        //高：删除的时候如果如果不存该cache，希望返回的是true。
        if (cacheID == null) {
            return true;
        }

        if (lv1CacheMap.get(cacheID) != null) {
            lv1CacheMap.remove(cacheID);
        } else if (lv2CacheMap.get(cacheID) != null) {
            lv2CacheMap.remove(cacheID);
        } else if (lv3CacheMap.get(cacheID) != null) {
            lv3CacheMap.remove(cacheID);
        } else if (globalCacheMap.get(cacheID) != null) {
            globalCacheMap.remove(cacheID);
        }

        return true;

    }
}
