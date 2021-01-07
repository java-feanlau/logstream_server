package com.boyitech.logstream.core.manager.cache;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.factory.CacheFactory;
import com.boyitech.logstream.core.setting.CacheSettings;
import com.boyitech.logstream.core.util.jdbc.DBUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class YSCacheManager extends BaseCacheManager {

    //创建默认的内存缓存
//	private final BaseCache lv2Cache = CacheFactory.createCache();
//	private final BaseCache lv3Cache = CacheFactory.createCache();

    @Override
    public String createLv1Cache() {

        BaseCache cache = CacheFactory.createCache();
        String cacheID = cache.getCacheId();
        Map<String, Object> map = new HashMap<>();
        map.put("cache_id", cacheID);
        map.put("cache_size", CacheSettings.MEMORYCACHESIZE.getValue());
        map.put("cache_lv", "1");
        try {
            DBUtil.insert("ys_cache", map);
        } catch (SQLException e) {
            LOGGER.error("Cache:" + cacheID + "持久化失败 " + e.getMessage());
        }
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

        BaseCache cache = CacheFactory.createCache();
        String cacheID = cache.getCacheId();
        Map<String, Object> map = new HashMap<>();
        map.put("cache_id", cacheID);
        map.put("cache_size", CacheSettings.MEMORYCACHESIZE.getValue());
        map.put("cache_lv", "2");
        try {
            DBUtil.insert("ys_cache", map);
        } catch (SQLException e) {
            LOGGER.error("Cache:" + cacheID + "持久化失败 " + e.getMessage());
        }
        lv2CacheMap.put(cacheID, cache);
        LOGGER.info("Cache:" + cacheID + ",创建成功");

        return cacheID;
    }

    @Override
    public BaseCache getLv2Cache(String cacheID) {
        if (cacheID == null) {
            return null;
        }
        if (lv2CacheMap.get(cacheID) == null) {
            return null;
        } else {
            return lv2CacheMap.get(cacheID);
        }

    }

    @Override
    public String createLv3Cache() {

        BaseCache cache = CacheFactory.createCache();
        String cacheID = cache.getCacheId();
        Map<String, Object> map = new HashMap<>();
        map.put("cache_id", cacheID);
        map.put("cache_size", CacheSettings.MEMORYCACHESIZE.getValue());
        map.put("cache_lv", "3");
        try {
            DBUtil.insert("ys_cache", map);
        } catch (SQLException e) {
            LOGGER.error("Cache:" + cacheID + "持久化失败 " + e.getMessage());
            return null;
        }
        lv3CacheMap.put(cacheID, cache);
        LOGGER.info("Cache:" + cacheID + ",创建成功");

        return cacheID;
    }

    @Override
    public BaseCache getLv3Cache(String cacheID) {
        if (cacheID == null) {
            return null;
        }
        if (lv3CacheMap.get(cacheID) == null) {
            return null;
        } else {
            return lv3CacheMap.get(cacheID);
        }
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
        if (stringObjectMap == null) {
            return;
        }
        String cache_id = (String) stringObjectMap.get("cache_id");
        int cache_size = (int) stringObjectMap.get("cache_size");
        int cache_lv = (int) stringObjectMap.get("cache_lv");
        switch (cache_lv) {
            case 1:
                lv1CacheMap.put(cache_id, CacheFactory.createCache(cache_id, cache_size));
                break;
            case 2:
                lv2CacheMap.put(cache_id, CacheFactory.createCache(cache_id, cache_size));
                break;
            case 3:
                lv3CacheMap.put(cache_id, CacheFactory.createCache(cache_id, cache_size));
                break;
        }
    }

    @Override
    public boolean destoryCache(String cacheID) {
        //高：删除的时候如果如果不存该cache，希望返回的是true。
        if(cacheID == null){
            return true;
        }
        BaseCache cache = null;
        if (lv1CacheMap.get(cacheID) != null) {
            cache = lv1CacheMap.remove(cacheID);
        } else if (lv2CacheMap.get(cacheID) != null) {
            cache = lv2CacheMap.remove(cacheID);
        } else if (lv3CacheMap.get(cacheID) != null) {
            cache = lv3CacheMap.remove(cacheID);
        } else if (globalCacheMap.get(cacheID) != null) {
            cache = globalCacheMap.remove(cacheID);
        }
        if (cache == null) {
            return true;
        }
        HashMap<String, Object> delMap = new HashMap<>();
        delMap.put("cache_id", cacheID);
        try {
            DBUtil.delete("ys_cache", delMap);
        } catch (SQLException e) {
            LOGGER.error("Cache:" + cacheID + "从数据库删除失败");
            return false;
        }
        LOGGER.info("Cache:" + cacheID + "，销毁成功");
        return true;

    }
}
