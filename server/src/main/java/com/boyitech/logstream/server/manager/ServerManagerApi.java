package com.boyitech.logstream.server.manager;

import javax.jws.WebParam;
import javax.jws.WebService;

@WebService
public interface ServerManagerApi {

    /**
     * @Author Eric Zheng
     * @Description ShipperWorker
     * @Date 17:10 2019/3/11
     **/
    String createShipperWorker(@WebParam(name = "shipperConfig") String shipperConfig,
                               @WebParam(name = "lv1CacheID") String lv1CacheID);

    String destroyShipperWorker(@WebParam(name = "shipperID") String shipperID);

    String startShipperWorker(@WebParam(name = "shipperID") String shipperID);

    String stopShipperWorker(@WebParam(name = "shipperID") String shipperID);

    /**
     * @Author Eric Zheng
     * @Description IndexerWorker
     * @Date 13:20 2019/3/14
     **/
    /*
    * @Author juzheng
    * @Description 对createIndexerWorker修改，新增ip过滤
    * @Date 1:37 PM 2019/8/7x
    */
    String createIndexerWorker(@WebParam(name = "indexerConfig") String indexerConfig,
                               @WebParam(name = "lv1CacheID") String lv1CacheID,
                               @WebParam(name = "lv2CacheID") String lv2CacheID);

    String startIndexerWorker(@WebParam(name = "indexerID") String indexerID);

    String stopIndexerWorker(@WebParam(name = "indexerID") String indexerID);

    String destroyIndexerWorker(@WebParam(name = "indexerID") String indexerID);

    /**
     * @Author Eric Zheng
     * @Description PorterWorker
     * @Date 16:54 2019/3/25
     **/
    String createPorterWorker(@WebParam(name = "porterConfig") String porterConfig,
                              @WebParam(name = "lv2CacheID") String lv2CacheID,
                              @WebParam(name = "lv3CacheID") String lv3CacheID);

    String createPorterWorkerlv2(@WebParam(name = "porterConfig") String porterConfig,
                              @WebParam(name = "lv2CacheID") String lv2CacheID,
                              @WebParam(name = "lv3CacheID") String lv3CacheID);

    String startPorterWorker(@WebParam(name = "porterID") String porterID);

    String stopPorterWorker(@WebParam(name = "porterID") String porterID);

    String destroyPorterWorker(@WebParam(name = "porterID") String porterID);

    String createCacheLv1();

    String createCacheLv2();

    String createCacheLv3();


    String destroyCache(@WebParam(name = "cacheID") String cacheID);

    /**
     * 获取当前系统中所有可用的日志格式化类型
     *
     * @return
     */
    String getAllLogTypes();

    /*
    * @Author juzheng
    * @Description 获得所有要geoip解析的ip字段名
    * @Date 3:59 PM 2019/8/13
    * @Param []
    * @return java.lang.String
    */
    String getAllIpFields();

    /*
    * @Author juzheng
    * @Description
    * @Date 4:40 PM 2019/8/15
    * @Param []
    * @return java.lang.String
    */
    String getAllIndexerInfos();

    String getAllWorkersStats();

    String getWorkerStatsByKey(@WebParam(name = "workerID") String workerID);

    String getAllCachesStats();

    String getCacheStatsByKey(@WebParam(name = "cacheID") String cacheID);

    String checkCacheByKey(@WebParam(name = "cacheID") String cacheID);

    String getShipperWorkerSpeed(@WebParam(name = "workerID") String workerID);
    String getIndexerWorkerSpeed(@WebParam(name = "workerID") String workerID);
    String getPorterWorkerSpeed(@WebParam(name = "workerID") String workerID);


    /**
     * 获取当前系统中所有可用的采集方式类型
     *
     * @return
     */
    String getAllShipperTypes();






    /**
     * @Author Eric Zheng
     * @Description 客户端shipper的赠删改
     * @Date 14:38 2019/3/27
     **/
    String createClientShipperWorker(@WebParam(name = "clientID") String clientID,
                                     @WebParam(name = "configJSON") String configJSON, @WebParam(name = "lv1CacheID") String lv1CacheID);

    String deleteClientShipperWorker(@WebParam(name = "clientID") String clientID,
                                     @WebParam(name = "shipperID") String shipperID);

    String updateClientShipperWorker(@WebParam(name = "clientID") String clientID,
                                     @WebParam(name = "shipperID") String shipperID,
                                     @WebParam(name = "configJSON") String configJSON,
                                     @WebParam(name = "lv1CacheID") String lv1CacheID);

    /**
     * 注册客户端
     *
     * @param clientID
     * @param fingerPrint
     * @return
     */
     boolean registerClient(@WebParam(name = "clientID") String clientID, @WebParam(name = "fingerPrint") String fingerPrint);

    /**
     * 获取所有注册客户端，返回结果为JSON格式
     *
     * @return
     */
    String getRegisteredClients();

    /**
     * 获取所有冲突客户端(一个客户端部署多个机器)，返回结果为JSON格式
     *
     * @return
     */
    String getUnregisteredClients();

    /**
     * 删除注册客户端
     *
     * @param clientID
     * @return
     */
    boolean deleteClient(@WebParam(name = "clientID") String clientID);

    /**
     * 获取客户端任务状态
     *
     * @return
     */
    public String getClientShipperConfig(@WebParam(name = "clientID") String clientID, @WebParam(name = "shipperID") String shipperID);

    /**
     * 获取worker最近的异常信息
     */
    public String getWorkerExceptionsByID(@WebParam(name = "workerID") String workerID);

    public String getServerID();
}
