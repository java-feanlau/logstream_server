package com.boyitech.logstream.server.manager.client;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.info.ClientShipperStatus;
import com.boyitech.logstream.core.info.ClientStatus;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.info.exception.ExceptionInfo;
import com.boyitech.logstream.core.manager.BaseManager;
import com.boyitech.logstream.core.setting.ClientSettings;
import com.boyitech.logstream.core.setting.SystemSettings;
import com.boyitech.logstream.core.util.EventFilterHelper;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.jdbc.DBUtil;
import com.boyitech.logstream.server.factory.SingleManagerFactory;
import com.google.gson.reflect.TypeToken;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerClientManager extends BaseManager implements LogServerClientManager {

    // 已注册的客户端<clientID, clientStatus>
    private final Map<String, ClientStatus> registered = new ConcurrentHashMap<>();
    // 已注册的客户端指纹信息<clientID , fingerPrint> 永远保持一一对应
    private final Map<String, String> registeredCP = new ConcurrentHashMap<>();
    // 冲突的客户端<fingerPrint, clientStatus>
    private final Map<String, ClientStatus> conflict = new ConcurrentHashMap<>();
    //shipperID->CacheID
    private final Map<String, String> shipper2Cache = new ConcurrentHashMap<>();
    //clientID->clientConfig
    private final Map<String, String> clientConfigMap = new ConcurrentHashMap<>();


    public void restart(List<Map<String, Object>> clientList, List<Map<String, Object>> clientShipperList) {
        if (clientList == null) {
            return;
        }
        //恢复客户端
        for (Map<String, Object> stringObjectMap : clientList) {
            String client_id = (String) stringObjectMap.get("client_id");
            String fingerPrint = (String) stringObjectMap.get("fingerPrint");
            int status = (int) stringObjectMap.get("status");

            ClientStatus clientStatus = new ClientStatus(client_id, fingerPrint);
            if (status == 0) {
                if (!"".equals(fingerPrint)) {
                    registeredCP.put(client_id, fingerPrint);
                }
                registered.put(client_id, clientStatus);
                LOGGER.info("恢复以注册客户端：" + clientStatus);
            } else if (status == 1) {
                //冲突
                conflict.put(fingerPrint, clientStatus);
                LOGGER.info("恢复冲突客户端：" + clientStatus);
            }
        }
        //恢复各个客户端相关配置文件
        for (Map<String, Object> stringObjectMap : clientShipperList) {
            String shipper_id = (String) stringObjectMap.get("shipper_id");
            String client_id = (String) stringObjectMap.get("client_id");
            String shipper_config = (String) stringObjectMap.get("shipper_config");
            String lv1cache = (String) stringObjectMap.get("lv1cache");
            ClientStatus clientStatus = registered.get(client_id);
            clientStatus.addClientShipperStatus(shipper_id, shipper_config);
            clientStatus.decreaseCasVersion();   //保证重启后casversion为0，从而使客户端强制更新配置文件
            shipper2Cache.put(shipper_id, lv1cache);
            EventFilterHelper.addShipperMark(shipper_id);
            clientConfigMap.put(client_id, clientStatus.getClientShipperConfigJson());
        }

    }


    /*
     * @Author Eric Zheng
     * @Description 1.注册一个客户端，第一次注册的时候fingerPrint为null。
     *              2.注册一个冲突客户端，此时保证指纹为冲突客户端所在的机器指纹，再指定一个新的clinetID
     * @Date 13:40 2019/6/27
     **/
    @Override
    public boolean registerClient(String clientID, String fingerPrint) {
        if (clientID == "" || clientID == null) {
            LOGGER.error("clientID不能为空");
            return false;
        } else if (fingerPrint == null) {
            fingerPrint = "";
        }


        if ((!registered.containsKey(clientID) && !registeredCP.containsValue(fingerPrint))) {
            //指纹为冲突客户端，从冲突队列删除该client
            if (conflict.containsKey(fingerPrint)) {
                HashMap<String, Object> delMap = new HashMap<>();
                delMap.put("fingerPrint", fingerPrint);
                try {
                    DBUtil.delete("ys_client", delMap);
                } catch (SQLException e) {
                    LOGGER.error("冲突client:" + clientID + "从数据库删除失败");
                    return false;
                }
                conflict.remove(fingerPrint);

            }
            LOGGER.debug("注册clientID:" + clientID + "的客户端");
            ClientStatus clientStatus = new ClientStatus(clientID, fingerPrint);

            Map<String, Object> map = new HashMap<>();
            map.put("client_id", clientID);
            map.put("fingerPrint", fingerPrint);
            map.put("status", ClientSettings.NOCONFLICT.getValue());
            try {
                DBUtil.insert("ys_client", map);
            } catch (SQLException e) {
                LOGGER.error("client:" + clientID + "持久化失败,创建失败: " + e.getMessage());
                return false;
            }

            registered.put(clientID, clientStatus);
            if (!"".equals(fingerPrint)) {
                registeredCP.put(clientID, fingerPrint);
            }
            clientConfigMap.put(clientID, "{}");
            LOGGER.info("client:" + clientID + ",持久化并且创建成功");
            return true;
        } else {
            LOGGER.error("注册失败，clientID:" + clientID + "已存在，或者该指纹fingerPrint:" + fingerPrint + "机器已注册客户端");
            return false;
        }

    }


    /**
     * 替换注册客户端的指纹,一般来说就是替换刚注册指纹为null的客户端
     *
     * @param clientID
     * @param fingerPrint
     * @return
     */
    public boolean updateClient(String clientID, String fingerPrint) {
        if (clientID == "" || clientID == null)
            throw new IllegalArgumentException("client不能为空");
        if (fingerPrint == "" || fingerPrint == null)
            throw new IllegalArgumentException("fingerPrint不能为空");
        Map<String, Object> map = new HashMap<>();
        map.put("fingerPrint", fingerPrint);
        Map<String, Object> whereMap = new HashMap<>();
        whereMap.put("client_id", clientID);
        try {
            DBUtil.update("ys_client", map, whereMap);
        } catch (SQLException e) {
            LOGGER.error("client:" + clientID + "更新指纹失败: " + e.getMessage());
            return false;
        }
        registered.get(clientID).changeFingerPrint(fingerPrint);
        registeredCP.replace(clientID, fingerPrint);
        clientConfigMap.put(clientID, "{}");
        return true;

    }


    /**
     * 删除某个客户端代理client
     *
     * @param clientID
     * @return
     */
    public boolean deleteClient(String clientID) {
        //高：删除的时候如果如果不存该worker，希望返回的是true。
        if ((clientID == null && clientID.equals("")) || !registered.containsKey(clientID)) {
            return true;
        }
        HashMap<String, Object> delMap = new HashMap<>();
        delMap.put("client_id", clientID);
        try {
            DBUtil.delete("ys_client", delMap);
            DBUtil.delete("ys_client_shipper", delMap);
        } catch (SQLException e) {
            LOGGER.error("client:" + clientID + "从数据库删除失败");
            return false;
        }

        ClientStatus remove = registered.remove(clientID);
        for (String shipperID : remove.getClientShipperStatusMap().keySet()) {
            shipper2Cache.remove(shipperID);
        }
        if (registeredCP.containsKey(clientID)) {
            registeredCP.remove(clientID);
        }

        Iterator<Map.Entry<String, ClientStatus>> iterator = conflict.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ClientStatus> next = iterator.next();
            if (next.getValue().getClientID().equals(clientID)) {
                iterator.remove();
            }
        }
        clientConfigMap.remove(clientID);
        LOGGER.info("client:" + clientID + "删除成功");

        return true;
    }

    /**
     * @Author Eric Zheng
     * @Description 创建客户端shipper
     * @Date 14:28 2019/3/26
     **/
    @Override
    public String createClientShipperWorker(String clientID, String configJSON, String lv1CacheID) {
        if (clientID == null && clientID.equals("")) {
            return null;
        } else if (!authClientID(clientID)) {
            LOGGER.error("已注册客户端不存在该客户端：" + clientID);
            return null;
        }

        String shipperID = UUID.randomUUID().toString();

        Map<String, Object> map = new HashMap<>();
        map.put("shipper_id", shipperID);
        map.put("client_id", clientID);
        map.put("shipper_config", configJSON);
        map.put("lv1cache", lv1CacheID);
        try {
            DBUtil.insert("ys_client_shipper", map);
        } catch (SQLException e) {
            LOGGER.error("ClientShipperWorker:" + shipperID + "持久化失败,创建失败: " + e.getMessage());
            return null;
        }

        shipper2Cache.put(shipperID, lv1CacheID);
        //任务表示就是一个shipperID，如果接受的数据的shipperid不存在，则忽略
        EventFilterHelper.addShipperMark(shipperID);
        ClientStatus clientStatus = registered.get(clientID);
        clientStatus.addClientShipperStatus(shipperID, configJSON);
        clientConfigMap.put(clientID, clientStatus.getClientShipperConfigJson());
        LOGGER.info("ClientShipperWorker:" + shipperID + "创建成功");
        return shipperID;
    }

    /**
     * @Author Eric Zheng
     * @Description 删除客户端shipper
     * @Date 14:28 2019/3/26
     **/
    @Override
    public boolean deleteClientShipperWorker(String client, String shipperID) {
        if ((client == null || client.equals("") || !authClientID(client))) {
            LOGGER.error("客户端：" + client + "不存在或为空");
            return false;
        } else if (shipperID == null | !shipper2Cache.containsKey(shipperID)) {
            //高：删除的时候如果如果不存该worker，希望返回的是true。
            return true;
        }

        ClientStatus clientStatus = registered.get(client);
        clientStatus.removeClientShipperStatus(shipperID);
        EventFilterHelper.removeShipperMark(shipperID);
        shipper2Cache.remove(shipperID);
        clientConfigMap.replace(client, clientStatus.getClientShipperConfigJson());
        LOGGER.info("ClientShipperWorker:" + shipperID + "删除成功");


        Map<String, Object> map = new HashMap<>();
        map.put("shipper_id", shipperID);
        try {
            DBUtil.delete("ys_client_shipper", map);
        } catch (SQLException e) {
            LOGGER.error("ClientShipperWorker数据库操作失败:" + shipperID + "删除失败: " + e.getMessage());
            return true;
        }

        return true;
    }

    /**
     * @Author Eric Zheng
     * @Description 修改客户端shipper
     * @Date 14:28 2019/3/26
     **/
    @Override
    public boolean updateClientShipperWorker(String clientID, String shipperID, String configJSON, String lv1CacheID) {
        if((clientID == null && clientID.equals("")) || shipperID == null
                || SingleManagerFactory.getCacheManager().getLv1Cache(lv1CacheID) == null){
            return false;
        }

        ClientStatus clientStatus = registered.get(clientID);
        if (clientStatus == null) {
            return false;
        }

        if (shipper2Cache.replace(shipperID, lv1CacheID) == null) {
            return false;
        } else if (!authClientID(clientID) && EventFilterHelper.hasShipperMark(shipperID)) {
            LOGGER.error("已注册客户端不存在该客户端：" + clientID);
            return false;
        }

        boolean result = clientStatus.updateClientShipperStatus(shipperID, configJSON);
        if (result == true) {

            Map<String, Object> map = new HashMap<>();
            map.put("shipper_config", configJSON);
            map.put("lv1cache", lv1CacheID);
            Map<String, Object> whereMap = new HashMap<>();
            whereMap.put("client_id", clientID);
            whereMap.put("shipper_id", shipperID);
            try {
                DBUtil.update("ys_client_shipper", map, whereMap);
            } catch (SQLException e) {
                LOGGER.error("更新客户端数据库失败shipperID：" + shipperID + e.getMessage());
                return false;
            }

            clientConfigMap.replace(clientID, clientStatus.getClientShipperConfigJson());
            LOGGER.info("ClientShipperWorker:" + shipperID + "更新成功");
        } else {
            LOGGER.error("更新客户端失败shipperID：" + shipperID);
        }
        return result;
    }


    //获取某个clientshipper的配置
    @Override
    public ClientShipperStatus getClientConfig(String clientID, String shipperID) {
        if ((clientID == null && clientID.equals("")) || shipperID == null) {
            return null;
        }
        if (registered.get(clientID) == null) {
            return null;
        }
        return registered.get(clientID).getClientShipperStatusByKey(shipperID);
    }

    /**
     * 检查client是否存在
     *
     * @param clientID
     * @return
     */
    public boolean authClientID(String clientID) {
        return registered.containsKey(clientID);
    }


    /**
     * 心跳处理
     *
     * @param clientID
     * @param
     * @return
     */
    @Override
    public String handleHeartBeat(String clientID, String srcIp, String content) {

        LOGGER.trace("收到心跳:\n" + content);

        Map heartBeat = GsonHelper.fromJson(content);
        String fingerPrint = heartBeat.get("fingerPrint").toString();
//        System.out.println(clientID + "==========================================" + fingerPrint);

        //客户端没有注册
        if (!authClientID(clientID)) {
            return "{\"http_status\": \"404\",\"clientID\":\"" + clientID + "\"}";
        } else {
            String registeredFingerPrint = registered.get(clientID).getFingerPrint();
            //接受的心跳指纹为空
            if (fingerPrint == null || fingerPrint == "") {
                return "{\"http_status\": \"400\",\"clientID\":\"" + clientID + "\"}";
            }
            //该客户端没有注册指纹
            if (registeredFingerPrint == null || registeredFingerPrint.equals("")) {
                //说明该指纹的机器已经注册了客户端。
                if (registeredCP.containsValue(fingerPrint)) {
                    return "{\"http_status\": \"421\",\"clientID\":\"" + clientID + "\"}";
                    //该客户端第一次注册指纹
                } else {
                    //将客户端指纹替换，并且持久化
                    this.updateClient(clientID, fingerPrint);
                    ClientStatus clientStatus = registered.get(clientID);
                    clientStatus.setSrcIP(srcIp);
                    clientStatus.setVersion((String) heartBeat.get("version"));
                    clientStatus.beat();
                    registeredCP.put(clientID, fingerPrint);
                    clientConfigMap.put(clientID, "{}");
                    LOGGER.debug("将" + fingerPrint + "注册为" + clientID + "的客户端实例");
                    return registered.get(clientID).toResponseHeartbeatBody();
                }
                //说明该客户端已经注册过别的机器
            } else if (!registeredFingerPrint.equals(fingerPrint)) {
                ClientStatus clientStatus;
                //首先检查是否已经更新了客户端id
                if (registeredCP.containsValue(fingerPrint)) {
                    Iterator<Map.Entry<String, String>> iterator = registeredCP.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, String> next = iterator.next();
                        if (next.getValue().equals(fingerPrint)) {
                            String newClientID = next.getKey();
                            return "{\"http_status\": \"201\",\"clientID\":\"" + newClientID + "\"}";
                        }
                    }
                } else if (!conflict.containsKey(fingerPrint)) {
                    LOGGER.debug("添加指纹：" + fingerPrint + " 到冲突列表");

                    Map<String, Object> map = new HashMap<>();
                    map.put("fingerPrint", fingerPrint);
                    map.put("client_id", clientID);
                    map.put("status", ClientSettings.CONFLICT.getValue());
                    try {
                        DBUtil.insert("ys_client", map);
                    } catch (SQLException e) {
                        LOGGER.error("冲突client:" + clientID + "持久化失败,创建失败: " + e.getMessage());
                    }

                    clientStatus = new ClientStatus(clientID, fingerPrint);
                    clientStatus.beat();
                    clientStatus.setSrcIP(srcIp);
                    conflict.put(fingerPrint, clientStatus);
                } else {
                    clientStatus = conflict.get(fingerPrint);
                    clientStatus.beat();
                    clientStatus.setSrcIP(srcIp);
                }
                return "{\"http_status\": \"422\",\"clientID\":\"" + clientID + "\"}";
                //正常心跳的执行逻辑代码块
            } else {
                ClientStatus clientStatus = registered.get(clientID);
                clientStatus.beat();
                String statusOfHeartbeatJson = (String) heartBeat.get("clientStatus");
                Map<String, Map> statusOfHeartbeat = GsonHelper.fromJsonMap(statusOfHeartbeatJson);

                Map<String, ClientShipperStatus> clientShipperStatusMap = clientStatus.getClientShipperStatusMap();
                for (Map.Entry<String, Map> entry : statusOfHeartbeat.entrySet()) {
                    String shipperID = entry.getKey();
                    Map statusMap = entry.getValue();
                    //服务端配置删除，可能客户端并没有及时更新，导致发送的心跳数据中还包含该shipperID
                    if (shipperID != null && clientShipperStatusMap.containsKey(shipperID)) {
                        clientShipperStatusMap.get(shipperID).setShipperID(shipperID);
                        if (statusMap.get("isRunning") != null) {
                            boolean isRunning = (boolean) statusMap.get("isRunning");
                            clientShipperStatusMap.get(shipperID).setRunning(isRunning);
                        }
                        if (statusMap.get("exceptions") != null) {
                            String exceptionsJson = statusMap.get("exceptions").toString();
                            ExceptionInfo[] array = GsonHelper.getGson().fromJson(exceptionsJson, new TypeToken<ExceptionInfo[]>() {
                            }.getType());
                            List<ExceptionInfo> exceptionsList = Arrays.asList(array);
                            ExceptionInfo[] exceptions = exceptionsList.toArray(new ExceptionInfo[SystemSettings.EXCEPTIONSlENGTH.getValue()]);
                            clientShipperStatusMap.get(shipperID).setExceptions(exceptions);
                        }
                    }
                }

                return clientStatus.toResponseHeartbeatBody();
            }

        }

    }

    /*
     * @Author Eric Zheng
     * @Description 接受采集代理数据
     * @Date 11:11 2019/6/26
     **/
    @Override
    public String handleBulk(String clientID, String srcIp, String content) {
        List<Map> bulk = null;
        try {
            bulk = GsonHelper.formJson(content);
        }
        catch (Exception e)
        {
            LOGGER.error("ServerClientManager.handleBulk："+e);
        }
        List result = new ArrayList();
        if (bulk.size() == 0) {
            return "{\"status\": \"204\"}";
        } else {
            for (Map e : bulk) {
                String shipperID = (String) e.get("mark");
                // 判断当前事件是否应该接收 并且mark就是shipperID
                if (shipperID == null | !shipper2Cache.containsKey(shipperID) | !EventFilterHelper.hasShipperMark(shipperID))
                    continue;
                // 写入到缓存
                Event event = new Event(e);
                event.setClientIP(srcIp);
                String cacheID = shipper2Cache.get(shipperID);
                BaseCache cache = SingleManagerFactory.getCacheManager().getLv1Cache(cacheID);

                if (cache != null && cache.offer(event)) {
                    // 写入lv1缓存成功
                    Map map = new HashMap();
                    map.put("status", "200");
                    result.add(map);
                } else if (cache == null) {
                    // 该类型缓存不存在
                    Map map = new HashMap();
                    map.put("status", "400");
                    map.put("reason", "illegal logType");
                    result.add(map);
                } else {
                    // 写入lv1缓存失败
                    Map map = new HashMap();
                    map.put("status", "503");
                    map.put("reason", "cache of " + event.getLogType() + " is full");
                    result.add(map);
                }
            }
            return GsonHelper.toJson(result);
        }
    }

    /**
     * @Author Eric Zheng
     * @Description 更新采集引擎配置文件
     * @Date 11:11 2019/6/26
     **/

    @Override
    public String handleConfig(String clientID, String srcIp, String content) {
        Map updateConfig = GsonHelper.fromJson(content);

        Double casVersion = (Double) updateConfig.get("casVersion");
        ClientStatus clientStatus = registered.get(clientID);
        if (clientStatus == null) {
            return "{\"http_status\": \"400\"}";
        } else {
            if (casVersion == clientStatus.getCasVersion()) {
                return "{\"http_status\": \"201\"}";
            } else {
                return clientStatus.toResponseConfigBody();
            }
        }
    }

    /*
     * @Author Eric Zheng
     * @Description 获取已经注册的客户端
     * @Date 9:37 2019/3/28
     **/
    public Map<String, ClientStatus> getRegisteredClients() {
        return registered;
    }

    public Map<String, String> getClientConfigMap() {
        return clientConfigMap;
    }

    public Map<String, String> getShipper2Cache() {
        return shipper2Cache;
    }

    /*
     * @Author Eric Zheng
     * @Description 获取冲突客户端
     * @Date 9:37 2019/3/28
     **/
    public Map<String, ClientStatus> getConflictClients() {
        return conflict;
    }


}
