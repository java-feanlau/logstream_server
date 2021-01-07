package com.boyitech.logstream.server.manager.client;

import com.boyitech.logstream.core.info.ClientShipperStatus;
import com.boyitech.logstream.core.info.ClientStatus;

import java.util.Map;

public interface LogServerClientManager {


    /**
     * 注册一对uuid和fingerPrint
     *
     * @param uuid
     * @param fingerPrint
     * @return
     */
    public boolean registerClient(String uuid, String fingerPrint);

    /**
     * 替换指定uuid客户端的指纹
     *
     * @param uuid
     * @param fingerPrint
     * @return
     */
    public boolean updateClient(String uuid, String fingerPrint);

    /**
     * 删除指定客户端注册记录
     *
     * @param uuid
     * @return
     */
    public boolean deleteClient(String uuid);


    /**
     * 获取所有已注册的客户端
     *
     * @return
     */
     Map<String, ClientStatus> getRegisteredClients();

    /**
     * 获取所有发现冲突的客户端
     *
     * @return
     */
     Map<String, ClientStatus> getConflictClients();


    /**
     * 处理心跳请求并返回响应
     *
     * @return
     */
    public String handleHeartBeat(String clientID, String srcIP, String content);

    /**
     * 处理bulk请求并返回响应
     *
     * @return
     */
    public String handleBulk(String clientID, String srcIP, String content);

    /**
     * 处理更新配置文将并返回响应
     *
     * @return
     */
    public String handleConfig(String clientID, String srcIp, String content);


    //_-------------------------------------
    /*
     * @Author Eric Zheng
     * @Description 赠删改一个客户端shipper
     * @Date 14:26 2019/3/27
     **/
    String createClientShipperWorker(String clientID, String configJSON, String lv1CacheID);

    boolean deleteClientShipperWorker(String clientID, String shipperID);

    boolean updateClientShipperWorker(String clientID, String shipperID, String configJSON, String lv1CacheID);

    /*
     * @Author Eric Zheng
     * @Description  获取指定客户端shipper的config
     * @Date 15:00 2019/3/27
     **/
    ClientShipperStatus getClientConfig(String clientID, String shipperID);
}
