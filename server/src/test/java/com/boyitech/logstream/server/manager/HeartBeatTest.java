package com.boyitech.logstream.server.manager;

import com.boyitech.logstream.core.util.ClientHelper;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.jdbc.DBUtil;
import com.boyitech.logstream.server.manager.client.ServerClientManager;
import com.sun.corba.se.impl.util.Version;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Eric
 * @Title: HeartBeatTest
 * @date 2019/4/19 10:06
 * @Description: TODO
 */
public class HeartBeatTest {
    ServerClientManager serverClientManager = null;
    String content = null;
    String content2 = null;
    String clientID = "1";
    String clientID2 ="2";
    String IP = null;
    String shipperID = null;

    @Before
    public void befor() {
        HashMap<String, Object> delMap = new HashMap<>();
        delMap.put("client_id", clientID);
        try {
            DBUtil.delete("ys_client", delMap);
            DBUtil.delete("ys_client_shipper", delMap);
        } catch (SQLException e) {
            Assert.assertTrue(false);
        }
        delMap.put("client_id", clientID2);
        try {
            DBUtil.delete("ys_client", delMap);
            DBUtil.delete("ys_client_shipper", delMap);
        } catch (SQLException e) {
            Assert.assertTrue(false);
        }

        serverClientManager = new ServerClientManager();
        //构建心跳
        shipperID = "111";
        IP = "1.1.1.1";

        Map map = new HashMap();
        Map tmp = new HashMap();
        tmp.put("shipperID", shipperID);
        tmp.put("isRunning", true);
        tmp.put("exceptions", new String[10]);
        map.put(shipperID, tmp);

        Map heartBeatInfo = new HashMap();
        heartBeatInfo.put("fingerPrint", ClientHelper.MACHINECODE);
        heartBeatInfo.put("version", Version.VERSION);
        heartBeatInfo.put("clientStatus", GsonHelper.toJson(map));

        content = GsonHelper.toJson(heartBeatInfo);


        Map heartBeatInfo2 = new HashMap();
        heartBeatInfo2.put("fingerPrint", "aaaaaaaaaaaa");
        heartBeatInfo2.put("version", Version.VERSION);
        heartBeatInfo2.put("clientStatus", GsonHelper.toJson(map));
        content2 = GsonHelper.toJson(heartBeatInfo2);
    }


    //客户端没有注册
    @Test
    public void testNotRegistClient() {
        //客户端没有
        String s = serverClientManager.handleHeartBeat(clientID, IP, content);
        Map<String, String> stringStringMap = GsonHelper.fromJson(s);
        Assert.assertTrue(stringStringMap.get("http_status") .equals("404") );
    }

    //一台机器启动多个客户端
    @Test
    public void testTwoClient() {
        serverClientManager.registerClient(clientID,null);
        serverClientManager.handleHeartBeat(clientID, IP, content);


        serverClientManager.registerClient(clientID2,null);

        String clientTwoResult= serverClientManager.handleHeartBeat(clientID2, IP, content);

        Map<String, String> stringStringMap = GsonHelper.fromJson(clientTwoResult);
        Assert.assertTrue(stringStringMap.get("http_status") .equals("421") );
    }



    //一个客户端在多台机器启动
    @Test
    public void testoneClientStartTwoMachine() {
        serverClientManager.registerClient(clientID,null);
        serverClientManager.handleHeartBeat(clientID, IP, content);

        String clientTwoResult= serverClientManager.handleHeartBeat(clientID, IP, content2);
        Map<String, String> stringStringMap = GsonHelper.fromJson(clientTwoResult);
        Assert.assertTrue(stringStringMap.get("http_status") .equals("422") );
    }


    //正常心跳收发
    @Test
    public void testNormalHeartbeat() {
        serverClientManager.registerClient(clientID,null);
        String result = serverClientManager.handleHeartBeat(clientID, IP, content);

        Map<String, String> stringStringMap = GsonHelper.fromJson(result);
        Assert.assertTrue(stringStringMap.get("http_status") .equals("200") );
    }
}

