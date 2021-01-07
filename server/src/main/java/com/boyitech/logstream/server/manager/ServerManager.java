package com.boyitech.logstream.server.manager;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.info.ClientShipperStatus;
import com.boyitech.logstream.core.info.ClientStatus;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.info.exception.ExceptionInfo;
import com.boyitech.logstream.core.manager.BaseManager;
import com.boyitech.logstream.core.manager.cache.BaseCacheManager;
import com.boyitech.logstream.core.manager.indexer.BaseIndexerManager;
import com.boyitech.logstream.core.manager.porter.BasePorterManager;
import com.boyitech.logstream.core.manager.shipper.BaseShipperManager;
import com.boyitech.logstream.server.factory.SingleManagerFactory;
import com.boyitech.logstream.server.manager.client.ServerClientManager;
import com.boyitech.logstream.server.manager.stats.StatsManager;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.LogStreamHelper;
import com.boyitech.logstream.core.util.jdbc.DBUtil;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.porter.BasePorter;
import com.boyitech.logstream.core.worker.shipper.BaseShipper;


import java.sql.SQLException;
import java.util.*;


@javax.jws.WebService(name = "LogProcessor", serviceName = "LogProcessor")
public class ServerManager extends BaseManager implements ServerManagerApi {

    private String serverID;
    private BaseShipperManager shipperManager;
    private BaseIndexerManager indexerManager;
    private BasePorterManager porterManager;
    private BaseCacheManager cacheManager;
    private ServerClientManager clientManager;
    private StatsManager statsManager;


    public ServerManager() {
        try {
            List<Map<String, Object>> query = DBUtil.query("select * from ys_server");
            if (query.size() == 0) {
                serverID = UUID.randomUUID().toString();
                Map<String, Object> map = new HashMap<>();
                map.put("server_id", serverID);
                DBUtil.insert("ys_server", map);
            } else {
                serverID = (String) query.get(0).get("server_id");
            }
        } catch (SQLException e) {
            LOGGER.error(e);
            LOGGER.error("ServerManager的初始化失败,初始化server_id失败");
        }

        shipperManager = SingleManagerFactory.getShipperManager();
        indexerManager = SingleManagerFactory.getIndexerManager();
        porterManager = SingleManagerFactory.getPorterManager();
        cacheManager = SingleManagerFactory.getCacheManager();
        clientManager = SingleManagerFactory.getServerClientManager();
        checkRestart();
    }

    /**
     * @Author Eric Zheng
     * @Description 创建一个ShipperWorker
     * @Date 9:52 2019/3/12
     **/
    @Override
    public String createShipperWorker(String shipperConfig, String lv1CacheID) {
        // 根据当前日志流的日志格式化类型获取lv1
        BaseCache lv1Cache = cacheManager.getLv1Cache(lv1CacheID);
        if (lv1Cache == null) {
            return "{\"soap_status\": \"400\"}";
        }
        BaseShipper shipper = shipperManager.createShipper(shipperConfig, lv1Cache);
        if (shipper == null) {
            return "{\"soap_status\": \"400\"}";
        }
        String shipperID = shipper.getWorkerId();
        return "{\"soap_status\": \"200\",\"shipperID\":\"" + shipperID + "\"}";
    }


    @Override
    public String startShipperWorker(String shipperID) {
        LOGGER.info("启动ShipperWorker:" + shipperID);
        if (shipperManager.startWorker(shipperID)) {
            statsManager.changeWorkerStatus(shipperID, "200");
            return "{\"soap_status\": \"200\"}";
        }
        return "{\"soap_status\": \"400\"}";
    }

    /*
     * @Author Eric Zheng
     * @Description 执行shipper中的teardown，并且停止对execute的方法调用
     **/
    @Override
    public String stopShipperWorker(String shipperID) {
        LOGGER.info("停止ShipperWorker:" + shipperID + "采集");
        if (shipperManager.stopWorker(shipperID)) {
            statsManager.changeWorkerStatus(shipperID, "400");
            return "{\"soap_status\": \"200\"}";
        }
        return "{\"soap_status\": \"400\"}";
    }


    /**
     * @Author Eric Zheng
     * @Description 如果shipper没有重写destroy方法，则该方法与停止作用一样，但是会将缓存和数据库的数据删除
     * 如果stop后能就shipper的代码逻辑执行完毕，则不需要重写的destory
     * @Date 16:48 2019/3/12
     **/
    @Override
    public String destroyShipperWorker(String shipperID) {
        LOGGER.info("删除ShipperWorker:" + shipperID);
        if (shipperManager.destroyWorker(shipperID)) {
            statsManager.clearShipperWorkerById(shipperID);
            return "{\"soap_status\": \"200\"}";
        }
        return "{\"soap_status\": \"400\"}";
    }

    @Override
    public String createIndexerWorker(String indexerConfig, String lv1CacheID, String lv2CacheID) {
        String indexerID = null;
        // 根据当前日志流的日志格式化类型获取lv1
        BaseCache lv1Cache = cacheManager.getLv1Cache(lv1CacheID);
        BaseCache lv2Cache = cacheManager.getLv2Cache(lv2CacheID);
        if (lv1Cache == null || lv2Cache == null) {
            return "{\"soap_status\": \"400\"}";
        }
        BaseIndexer indexer = indexerManager.createIndexer(indexerConfig, lv1Cache, lv2Cache);
        if (indexer == null) {
            return "{\"soap_status\": \"400\"}";
        }

        indexerID = indexer.getWorkerId();

        return "{\"soap_status\": \"200\",\"indexerID\":\"" + indexerID + "\"}";
    }

    @Override
    public String startIndexerWorker(String indexerID) {
        LOGGER.info("启动IndexerWorker:" + indexerID);
        if (indexerManager.startWorker(indexerID)) {
            statsManager.changeWorkerStatus(indexerID, "200");
            return "{\"soap_status\": \"200\"}";
        }

        return "{\"soap_status\": \"400\"}";
    }

    @Override
    public String stopIndexerWorker(String indexerID) {
        LOGGER.info("停止IndexerWorker:" + indexerID + "采集");
        if (indexerManager.stopWorker(indexerID)) {
            statsManager.changeWorkerStatus(indexerID, "400");
            return "{\"soap_status\": \"200\"}";
        }
        return "{\"soap_status\": \"400\"}";
    }


    @Override
    public String destroyIndexerWorker(String indexerID) {
        LOGGER.info("删除IndexerWorker:" + indexerID);
        if (indexerManager.destroyWorker(indexerID)) {
            statsManager.clearIndexerWorkerById(indexerID);
            return "{\"soap_status\": \"200\"}";
        }
        return "{\"soap_status\": \"400\"}";
    }

    @Override
    public String createPorterWorker(String porterConfig, String lv2CacheID, String lv3CacheID) {
        String porterID = null;
        // 根据当前日志流的日志格式化类型获取lv1
        BaseCache lv2Cache = cacheManager.getLv2Cache(lv2CacheID);
        BaseCache lv3Cache = null;

        if (lv2Cache == null) {
            return "{\"soap_status\": \"400\"}";
        } else if (lv3CacheID != null && !lv3CacheID.equals("")) {
            lv3Cache = cacheManager.getLv3Cache(lv3CacheID);
            if (lv3Cache == null) {
                return "{\"soap_status\": \"400\"}";
            }
        }


        BasePorter porter = porterManager.createPorter(porterConfig, lv2Cache, lv3Cache);
        if (porter == null) {
            return "{\"soap_status\": \"400\"}";
        }
        porterID = porter.getWorkerId();
        this.startPorterWorker(porterID);
        return "{\"soap_status\": \"200\",\"porterID\":\"" + porterID + "\"}";
    }

    @Override
    public String createPorterWorkerlv2(String porterConfig, String lv2CacheID, String lv3CacheID) {
        String porterID = null;
        // 根据当前日志流的日志格式化类型获取lv1
        BaseCache lv2Cache = cacheManager.getLv2Cache(lv2CacheID);
        BaseCache lv3Cache = null;

        if (lv2Cache == null) {
            return "{\"soap_status\": \"400\"}";
        } else if (lv3CacheID != null && !lv3CacheID.equals("")) {
            lv3Cache = cacheManager.getLv3Cache(lv3CacheID);
            if (lv3Cache == null) {
                return "{\"soap_status\": \"400\"}";
            }
        }
        BasePorter porter = porterManager.createPorter(porterConfig, lv2Cache, lv3Cache);
        if (porter == null) {
            return "{\"soap_status\": \"400\"}";
        }
        porterID = porter.getWorkerId();

        return "{\"soap_status\": \"200\",\"porterID\":\"" + porterID + "\"}";
    }

    @Override
    public String startPorterWorker(String porterID) {
        LOGGER.info("启动PorterWorker:" + porterID);
        if (porterManager.startWorker(porterID))
            return "{\"soap_status\": \"200\"}";
        return "{\"soap_status\": \"400\"}";
    }

    @Override
    public String stopPorterWorker(String porterID) {
        LOGGER.info("停止PorterWorker:" + porterID);
        if (porterManager.stopWorker(porterID)) {
            return "{\"soap_status\": \"200\"}";
        }
        return "{\"soap_status\": \"400\"}";
    }

    @Override
    public String destroyPorterWorker(String porterID) {
        LOGGER.info("删除PorterWorker:" + porterID);
        if (porterManager.destroyWorker(porterID)) {
            statsManager.clearPorterWorkerById(porterID);
            return "{\"soap_status\": \"200\"}";
        }
        return "{\"soap_status\": \"400\"}";
    }


    /*
     * @Author Eric Zheng
     * @Description cache相关操作
     * @Date 11:11 2019/4/1
     **/
    @Override
    public String createCacheLv1() {
        return cacheManager.createLv1Cache();
    }

    @Override
    public String createCacheLv2() {
        return cacheManager.createLv2Cache();
    }

    @Override
    public String createCacheLv3() {
        return cacheManager.createLv3Cache();
    }

    @Override
    public String destroyCache(String cacheID) {
        boolean result = cacheManager.destoryCache(cacheID);
        if (result) {
            return "{\"soap_status\": \"200\"}";
        }
        return "{\"soap_status\": \"400\"}";
    }

    /*
     * @Author Eric Zheng
     * @Description 采集引擎支持的indexer类型
     * @Date 11:33 2019/6/27
     **/
    @Override
    public String getAllLogTypes() {
        return GsonHelper.toJson(LogStreamHelper.getLogTypes());
    }

    /*
     * @Author juzheng
     * @Description 获得所有要geoip解析的字段名
     * @Date 4:00 PM 2019/8/13
     * @Param []
     * @return java.lang.String
     */
    @Override
    public String getAllIpFields() {
        return GsonHelper.toJson(LogStreamHelper.getIPFields());
    }

    @Override
    public String getAllIndexerInfos() {
        return GsonHelper.toJson(LogStreamHelper.getAllIndexerInfos());
    }


    /*
     * @Author Eric Zheng
     * @Description 采集引擎支持的shipper类型
     * @Date 11:33 2019/6/27
     **/
    @Override
    public String getAllShipperTypes() {
        return GsonHelper.toJson(LogStreamHelper.getServerShipperTypes());
    }

    /*
     * @Author Eric Zheng
     * @Description 从缓存里拿一个Event出来看看是什么东西
     * @Date 16:09 2019/6/27
     **/
    @Override
    public String checkCacheByKey(String cacheID) {

        BaseCache cache = cacheManager.getLv1Cache(cacheID);
        if (cache == null) {
            cache = cacheManager.getLv2Cache(cacheID);
            if (cache == null) {
                cache = cacheManager.getLv3Cache(cacheID);
                if (cache == null) {
                    return "{}";
                }
            }
        }
        Event event;
        Optional<List> optional = Optional.ofNullable(cache.poll(1));
        if (!optional.isPresent() || optional.filter(a -> a.size() == 0).isPresent()) {
            return "{}";
        } else {
            List list = optional.orElseGet(() -> new ArrayList());
            event = (Event) list.get(0);
            return GsonHelper.toJson(event.getMessage());
        }


    }

    /*
     * @Author Eric Zheng
     * @Description 有延迟，并且返回包括采集代理的worker信息
     * @Date 16:11 2019/6/27
     **/
    @Override
    public String getAllWorkersStats() {
        return statsManager.getAllWorkersStats();
    }

    /*
     * @Author Eric Zheng
     * @Description 实时性,包括采集代理，但是采集代理的信息并不是实时性的，会有一个心跳的延迟
     * @Date 16:11 2019/6/27
     **/
    @Override
    public String getWorkerStatsByKey(String workerID) {
        BaseShipper shipper = shipperManager.getShipperById(workerID);
        if (shipper != null) {
            String result = shipper.isAlive() == true ? "200" : "400";
            return result;
        }
        BaseIndexer indexer = indexerManager.getIndexerById(workerID);
        if (indexer != null) {
            String result = indexer.isAlive() == true ? "200" : "400";
            return result;
        }
        BasePorter porter = porterManager.getPorterById(workerID);
        if (porter != null) {
            String result = porter.isAlive() == true ? "200" : "400";
            return result;
        }
        Map<String, ClientStatus> registered = clientManager.getRegisteredClients();
        //遍历所有采集代理，安全
        for (ClientStatus clientStatus : registered.values()) {
            //遍历采集代理所有clieWorkerShipper，安全
            for (ClientShipperStatus clientShipperStatus : clientStatus.getClientShipperStatusMap().values()) {
                if (clientShipperStatus.getShipperID().equals(workerID)) {
                    String result = clientShipperStatus.isRunning() == true ? "200" : "400";
                    return result;
                }
            }
        }
        return "404";
    }

    @Override
    public String getAllCachesStats() {
        return statsManager.getAllCachesStats();
    }

    @Override
    public String getCacheStatsByKey(String cacheID) {
        return statsManager.getCacheStatsByKey(cacheID);
    }


    @Override
    public String getShipperWorkerSpeed(String workerID) {
        return shipperManager.getShipperWorkerSpeed(workerID);
    }

    @Override
    public String getIndexerWorkerSpeed(String workerID) {
        return indexerManager.getIndexerWorkerSpeed(workerID);
    }

    @Override
    public String getPorterWorkerSpeed(String workerID) {
        return porterManager.getPorterWorkerSpeed(workerID);
    }


    //注册客户端
    @Override
    public boolean registerClient(String uuid, String fingerPrint) {
        return clientManager.registerClient(uuid, fingerPrint);
    }

    //获取以注册客户端
    @Override
    public String getRegisteredClients() {
        return GsonHelper.toJson(clientManager.getRegisteredClients());
    }

    //获取冲突客户端
    @Override
    public String getUnregisteredClients() {
        return GsonHelper.toJson(clientManager.getConflictClients());
    }


    //删除客户端
    @Override
    public boolean deleteClient(String clientID) {
        return clientManager.deleteClient(clientID);
    }


    @Override
    public String createClientShipperWorker(String clientID, String configJSON, String lv1CacheID) {
        LOGGER.debug("创建ClientShipperWorker：" + clientID);
        BaseCache lv1Cache = cacheManager.getLv1Cache(lv1CacheID);
        if (lv1Cache == null) {
            LOGGER.error(lv1CacheID + "对应的cache不存在，创建失败");
            return "{\"soap_status\": \"400\"}";
        }
        String clientShipperWorkerID = clientManager.createClientShipperWorker(clientID, configJSON, lv1CacheID);
        if (clientShipperWorkerID == null) {
            return "{\"soap_status\": \"400\"}";
        }
        return "{\"soap_status\": \"200\",\"clientShipperWorkerID\":\"" + clientShipperWorkerID + "\"}";
    }

    @Override
    public String deleteClientShipperWorker(String clientID, String shipperID) {
        LOGGER.debug("删除ClientShipperWorker：" + clientID);
        if (clientManager.deleteClientShipperWorker(clientID, shipperID)) {
            return "{\"soap_status\": \"200\"}";
        }
        return "{\"soap_status\": \"400\"}";
    }

    @Override
    public String updateClientShipperWorker(String clientID, String shipperID, String configJSON, String lv1CacheID) {
        if (clientManager.updateClientShipperWorker(clientID, shipperID, configJSON, lv1CacheID)) {
            LOGGER.debug("修改ClientShipperWorker：" + clientID);
            return "{\"soap_status\": \"200\"}";
        }
        LOGGER.error("修改ClientShipperWorker：" + clientID + "失败,原因为从传入的参数有误");
        return "{\"soap_status\": \"400\"}";
    }


    //获取客户端任务
    @Override
    public String getClientShipperConfig(String clientID, String shipperID) {
        ClientShipperStatus task = clientManager.getClientConfig(clientID, shipperID);
        String result = GsonHelper.toJson(task);
        return result;
    }



    /*
     * @Author Eric Zheng
     * @Description 重新启动的自检
     * @Date 14:30 2019/3/20
     **/

    public void checkRestart() {
        List<Map<String, Object>> cacheList = null;
        List<Map<String, Object>> porterList = null;
        List<Map<String, Object>> indexerList = null;
        List<Map<String, Object>> shipperList = null;
        List<Map<String, Object>> clientShipperList = null;
        List<Map<String, Object>> clientList = null;
        try {
            cacheList = DBUtil.query("select * from ys_cache");
            porterList = DBUtil.query("select * from ys_worker_porter");
            indexerList = DBUtil.query("select * from ys_worker_indexer");
            shipperList = DBUtil.query("select * from ys_worker_shipper");
            clientShipperList = DBUtil.query("select * from ys_client_shipper");
            clientList = DBUtil.query("select * from ys_client");
        } catch (SQLException e) {
            e.printStackTrace();
        }


        for (Map<String, Object> stringObjectMap : cacheList) {
            cacheManager.restart(stringObjectMap);
        }

        for (Map<String, Object> stringObjectMap : porterList) {
            porterManager.restart(cacheManager, stringObjectMap);
        }

        for (Map<String, Object> stringObjectMap : indexerList) {
            indexerManager.restart(cacheManager, stringObjectMap);
        }
        for (Map<String, Object> stringObjectMap : shipperList) {
            shipperManager.restart(cacheManager, stringObjectMap);
        }
        clientManager.restart(clientList, clientShipperList);

        //重启恢复数据以后再启动statusmanager
        statsManager = new StatsManager(shipperManager, indexerManager,
                porterManager, cacheManager, clientManager);

    }


    /*
     * @Author Eric Zheng
     * @Description 采集引擎和采集代理都是使用该方法获取异常
     * @Date 14:58 2019/5/21
     **/
    @Override
    public String getWorkerExceptionsByID(String workerID) {
        BaseShipper shipper = shipperManager.getShipperById(workerID);
        BaseIndexer indexer = indexerManager.getIndexerById(workerID);
        BasePorter porter = porterManager.getPorterById(workerID);
        ExceptionInfo[] lastExceptions = null;
        if (shipper != null) {
            lastExceptions = shipper.getLastExceptions();
        } else if (indexer != null) {
            lastExceptions = indexer.getLastExceptions();
        } else if (porter != null) {
            lastExceptions = porter.getLastExceptions();
        }
        Map<String, ClientStatus> registered = clientManager.getRegisteredClients();
        //遍历所有采集代理，安全
        try {
            for (ClientStatus clientStatus : registered.values()) {
                //遍历采集代理所有clieWorkerShipper，安全
                for (ClientShipperStatus clientShipperStatus : clientStatus.getClientShipperStatusMap().values()) {
                    if (clientShipperStatus.getShipperID().equals(workerID)) {
                        lastExceptions = clientShipperStatus.getExceptions();
                    }
                }
            }
        } catch (NullPointerException e) {
            return "{}";
        }

        return GsonHelper.toJson(lastExceptions);
    }

    @Override
    public String getServerID() {
        return serverID;
    }

    /*
     * @Author Eric Zheng
     * @Description 提供给ServerClientManager
     * @Date 14:27 2019/6/27
     **/


    @Override
    public void exit() {

    }

}