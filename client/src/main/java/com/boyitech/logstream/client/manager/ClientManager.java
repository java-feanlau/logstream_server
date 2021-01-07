package com.boyitech.logstream.client.manager;

import com.boyitech.logstream.client.factory.ManagerFactory;
import com.boyitech.logstream.client.info.Version;
import com.boyitech.logstream.client.manager.cache.YSClientCacheManager;
import com.boyitech.logstream.client.manager.shipper.YSClientShipperManager;
import com.boyitech.logstream.client.rest.config.UpdateConfigCreator;
import com.boyitech.logstream.client.setting.HeartBeatSetting;
import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.info.exception.ExceptionInfo;
import com.boyitech.logstream.core.info.exception.ExceptionLevel;
import com.boyitech.logstream.core.manager.BaseManager;
import com.boyitech.logstream.core.manager.cache.BaseCacheManager;
import com.boyitech.logstream.core.manager.shipper.BaseShipperManager;
import com.boyitech.logstream.core.setting.ClientSettings;
import com.boyitech.logstream.core.util.ClientHelper;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.porter.BasePorter;
import com.boyitech.logstream.core.worker.porter.BasePorterConfig;
import com.boyitech.logstream.core.worker.porter.client.ClientPorter;
import com.boyitech.logstream.core.worker.shipper.BaseShipper;
import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;

import javax.net.ssl.SSLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class ClientManager extends BaseManager implements LogClientManager {
    private final BaseShipperManager shipperManager;
    private final BaseCacheManager cacheManager;
    //	private final BasePorterManager porterManager;
    private final BasePorter porter;
    private final BaseCache clientCache;
    // shipperID->BaseShipperConfigString
    private Map<String, String> configMap;


    //丢失的心跳次数
    private int loseHeartbeatNumber = 0;


    //正常心跳的次数，在第一次心跳的时候，开启请求配置文件
    private int heartBeatNumer = 1;

    public ClientManager() {
        cacheManager = ManagerFactory.getYSClientCacheManager();
        shipperManager = ManagerFactory.getYSClientShipperManager();
//        porterManager = new YSClientPorterManager();
        String clientCacheID = cacheManager.createLv1Cache();
        clientCache = cacheManager.getLv1Cache(clientCacheID);

        Map<String, String> clientPorterConfig = new HashMap<>();
        clientPorterConfig.put("moduleType", "client");
        BasePorterConfig porterConfig = new BasePorterConfig(clientPorterConfig);
        porter = new ClientPorter(porterConfig);
        porter.setLv2Cache(clientCache);
        porter.doStart();

        this.configMap = new HashMap<>();
        LOGGER.info("采集客户端初始化完成");
    }


    /*
     * @Author Eric Zheng
     * @Description 心跳回包
     * @Date 9:41 2019/3/28
     **/
    @Override
    public void handleHeartBeatResponse(String heartBeat) {
        //LOGGER.debug("心跳回包:\n"+heartBeat);

        Map map = GsonHelper.fromJson(heartBeat);

        String http_status = (String) map.get("http_status");
        String clientID = (String) map.get("clientID");

        if (http_status.equals("404")) {
            LOGGER.error("该客户端没有注册，服务端拒绝访问");
            loseHeartbeatNumber++;
            if (loseHeartbeatNumber >= Integer.parseInt(HeartBeatSetting.maxLoseHeartbeatNumber.getValue())) {
                //todo 关闭客户端
                LOGGER.error("无响应次数超过" + HeartBeatSetting.maxLoseHeartbeatNumber.getValue() + "次，关闭客户端");
                System.exit(0);
            }
            return;
        } else if (http_status.equals("400")) {
            LOGGER.error("心跳数据发送错误，指纹为空 ，server拒绝接受");
            return;
        } else if (http_status.equals("421")) {
            LOGGER.error("该机器已注册客户端，server拒绝接受:" + clientID);
            return;
        } else if (http_status.equals("422")) {
            LOGGER.error("该客户端已在其他机器注册，进入冲突队列:" + clientID);
            return;
        } else if (http_status.equals("201")) {
            LOGGER.debug("放弃与其他机器客户端竞争相同ID，更新clientID为: " + map.get("clientID"));
            ClientSettings.setClientID(map.get("clientID").toString());
        } else if (http_status.equals("200")) {
            loseHeartbeatNumber = 0;
            if (heartBeatNumer == 1) {
                LOGGER.debug("心跳正常！开始更新配置文件，更新周期：" + HeartBeatSetting.updateConfigTime.getValue() + "毫秒");
                try {
                    // 创建发送心跳的对象并放入单独线程运行
                    UpdateConfigCreator creater = new UpdateConfigCreator(ClientSettings.getServerAddr());
                    new Thread(creater).start();
                } catch (SSLException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                heartBeatNumer++;
            }
            LOGGER.debug("心跳正常！");

        }
    }

    @Override
    public void handleUpdateConfigResponse(String heartConfig) {
        Map map = GsonHelper.fromJson(heartConfig);
        String http_status = (String) map.get("http_status");
        if (http_status.equals("400")) {
            LOGGER.error("尝试新配置文件失败,服务端没有注册该客户端");
            return;
        } else if (http_status.equals("201")) {
            LOGGER.debug("客户端配置文件没有更新！");
            return;
        } else {
            if (map.get("fingerPrint") == null) {
                LOGGER.info("服务端还未注册指纹");
                return;
            }

            if (!map.get("fingerPrint").toString().equals(ClientHelper.MACHINECODE)) {
                LOGGER.error("服务端指纹与该机器指纹不一致");
                return;
            }
            HashMap<String, BaseShipperConfig> newConfigMap = new HashMap<>();
            //更新了配置文件(shipperID -> ShipperConfigString)
            Map<String, String> newConfig = (Map) map.get("shipperConfigs");
            LOGGER.debug("新的配置：" + newConfig);
            LOGGER.debug("原有配置：" + configMap);
            LOGGER.info("配置发生变化，更新ClientWorker");
            //更新配置：删除了shipper
            for (String oldShipperID : configMap.keySet()) {
                if (!newConfig.containsKey(oldShipperID)) {
                    this.destroyClientShipper(oldShipperID);
                }
                if(configMap.size()==0){
                    break;
                }
            }
            //更新配置：新增和修改shipper
            for (Entry<String, String> entry : newConfig.entrySet()) {
                String shipperID = entry.getKey();
                String newconfigString = entry.getValue();
                //老的配置包含当前的新的shipperID
                if (configMap.containsKey(shipperID)) {
                    String oldShipperConfigString = configMap.get(shipperID);
                    //该shipperID配置发生了变化
                    if (!oldShipperConfigString.equals(newconfigString)) {
                        this.updateClientShipper(shipperID, newconfigString);
                    } else {
                        //shipper配置没有更新，则跳过
                        continue;
                    }
                    //说明新增的配置
                } else {
                    try {
                        this.createClientShipper(shipperID, newconfigString);
                    } catch (Exception e) {
                        LOGGER.error("创建ClientShipper：" + shipperID + "失败:" + e.getMessage());
                    }
                }

            }
            //修改版本号
            double casVersion = (double) map.get("casVersion");
            Version.setCASVERSION(casVersion);
        }

    }

    /*
     * @Author Eric Zheng
     * @Description 修改客户端shipper，具体为：删除老的，创建新的
     * @Date 9:51 2019/3/29
     **/
    private void updateClientShipper(String shipperID, String newConfig) {
        shipperManager.destroyWorker(shipperID);
//        BaseShipper shipper = shipperManager.createShipper(shipperID, newConfig, clientCache);
        BaseShipper shipper = null;
        try {
            createClientShipper(shipperID, newConfig);
        } catch (Exception e) {
            LOGGER.error("ClientShipperWorker:" + shipperID + "修改失败");
            configMap.put(shipperID, newConfig);
        }
        if (shipper != null) {
            configMap.put(shipperID, newConfig);
            shipper.doStart();
            LOGGER.info("ClientShipperWorker:" + shipperID + "修改成功");
        }

    }

    private void destroyClientShipper(String shipperID) {
        synchronized (configMap) {
            shipperManager.destroyWorker(shipperID);
            configMap.remove(shipperID);
        }
        LOGGER.info("ClientShipperWorker:" + shipperID + "删除成功");
    }

    private void createClientShipper(String shipperID, String newConfig) throws Exception {
        synchronized (configMap) {

            // 创建shipper并设置lv1缓存，所有shipper共用一个lv1cache
            configMap.put(shipperID, newConfig);
            BaseShipper shipper = shipperManager.createShipper(shipperID, newConfig, clientCache);

            if (shipper == null) {
                throw new Exception();
            }
            shipper.doStart();
            LOGGER.info("ClientShipperWorker:" + shipperID + "创建完成");

        }
    }


    @Override
    public void exit() {
        // TODO Auto-generated method stub

    }

    @Override
    public String getClientShipperStatus() {
        Map<String, BaseShipper> shipperMap = shipperManager.getShipperMap();
        //todo 返回的就每个worker的健康状态和错误信息
        Map map = new HashMap();
        Iterator<Entry<String, String>> iterator = configMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            String shipperID = entry.getKey();
            BaseShipper shipper = shipperMap.get(shipperID);
            if (shipper == null) {
                Map tmp = new HashMap();
                tmp.put("shipperID", shipperID);
                tmp.put("isRunning", false);
                ExceptionInfo[] exceptionInfo = new ExceptionInfo[10];
                exceptionInfo[0]=new ExceptionInfo(ExceptionLevel.MAJOR, "worker创建异常");
                tmp.put("exceptions", exceptionInfo);
                map.put(shipperID, tmp);
                continue;
            } else {
                Map tmp = new HashMap();
                tmp.put("shipperID", shipperID);
                tmp.put("isRunning", shipper.isAlive());
                tmp.put("exceptions", shipper.getLastExceptions());
                map.put(shipperID, tmp);
            }

        }

        return GsonHelper.toJson(map);
    }

    //for test
    public int getLoseHeartbeatNumber() {
        return loseHeartbeatNumber;
    }

    //for test
    public int getHeartBeatNumer() {
        return heartBeatNumer;
    }
}
