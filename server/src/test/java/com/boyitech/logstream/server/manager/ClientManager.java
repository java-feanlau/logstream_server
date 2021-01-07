package com.boyitech.logstream.server.manager;

import com.boyitech.logstream.core.info.ClientStatus;
import com.boyitech.logstream.core.util.jdbc.DBUtil;
import com.boyitech.logstream.server.manager.client.ServerClientManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eric
 * @Title: ClientManager
 * @date 2019/4/18 16:19
 * @Description: TODO
 */
public class ClientManager {
    ServerClientManager serverClientManager = null;

    @Before
    public void befor() {
        serverClientManager = new ServerClientManager();
        HashMap<String, Object> delMap = new HashMap<>();
        delMap.put("client_id", "123");
        try {
            DBUtil.delete("ys_client", delMap);
            DBUtil.delete("ys_client_shipper", delMap);
        } catch (SQLException e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void restartTest() {
        List<Map<String, Object>> clientShipperList = new ArrayList<>();
        List<Map<String, Object>> clientList = new ArrayList<>();

        Map<String, Object> clientMap = new HashMap();
        clientMap.put("client_id", "123");
        clientMap.put("fingerPrint", "aaa");
        clientMap.put("status", 0);
        clientList.add(clientMap);

        Map<String, Object> clienShippertMap = new HashMap();
        clienShippertMap.put("shipper_id", "321");
        clienShippertMap.put("client_id", "123");
        clienShippertMap.put("shipper_config", "{\"a\":\"1\"}");
        clienShippertMap.put("lv1cache", "111");
        clientShipperList.add(clienShippertMap);


        serverClientManager.restart(clientList, clientShipperList);


        Map<String, ClientStatus> registeredClients = serverClientManager.getRegisteredClients();
        Map<String, String> clientConfigMap = serverClientManager.getClientConfigMap();
//        System.out.println(registeredClients);
//        System.out.println(clientConfigMap);

        Assert.assertTrue(registeredClients.containsKey("123"));
        Assert.assertTrue(clientConfigMap.get("123").contains("321"));

    }


    @Test
    public void testRegisted() {
        serverClientManager.registerClient("123", "aaa");
        Assert.assertTrue(serverClientManager.getRegisteredClients().get("123").getFingerPrint() == "aaa");
    }

    @Test
    public void testUpdateClient() {
        serverClientManager.registerClient("123", "aaa");
        Assert.assertTrue(serverClientManager.getRegisteredClients().get("123").getFingerPrint() == "aaa");
        serverClientManager.updateClient("123", "bbb");
        Assert.assertTrue(serverClientManager.getRegisteredClients().get("123").getFingerPrint() == "bbb");
    }


    @Test
    public void testDeleteClient() {
        serverClientManager.registerClient("123", "aaa");
        Assert.assertTrue(serverClientManager.getRegisteredClients().get("123").getFingerPrint() == "aaa");
        serverClientManager.deleteClient("123");
        Assert.assertTrue(serverClientManager.getRegisteredClients().get("123") == null);
    }


    @Test
    public void testcreateClientShipperWorker() {
        serverClientManager.registerClient("123", "aaa");
        Assert.assertTrue(serverClientManager.getRegisteredClients().get("123").getFingerPrint() == "aaa");
        serverClientManager.createClientShipperWorker("123", "{\"a\":\"n\"}", "123");
        Assert.assertTrue(serverClientManager.getClientConfigMap().get("123").contains("\"a\":\"n\""));
    }

    @Test
    public void testdeleteClientShipperWorker() {
        serverClientManager.registerClient("123", "aaa");
        Assert.assertTrue(serverClientManager.getRegisteredClients().get("123").getFingerPrint() == "aaa");
        serverClientManager.createClientShipperWorker("123", "{\"a\":\"n\"}", "123");
        String shipperid = serverClientManager.getRegisteredClients().get("123")
                .getClientShipperStatusMap().keySet().iterator().next();
        serverClientManager.deleteClientShipperWorker("123", shipperid);
        Assert.assertTrue(serverClientManager.getRegisteredClients().get("123").getClientShipperStatusMap().size() == 0);
    }

    @Test
    public void testupdateClientShipperWorker() {
        serverClientManager.registerClient("123", "aaa");
        Assert.assertTrue(serverClientManager.getRegisteredClients().get("123").getFingerPrint() == "aaa");
        serverClientManager.createClientShipperWorker("123", "{\"a\":\"n\"}", "123");
        String shipperid = serverClientManager.getRegisteredClients().get("123")
                .getClientShipperStatusMap().keySet().iterator().next();
        serverClientManager.updateClientShipperWorker("123", shipperid, "{\"a\":\"b\"}", "321");
        System.out.println();
        Assert.assertTrue(serverClientManager.getRegisteredClients().get("123").getClientShipperConfigJson().contains("\"a\":\"b\""));
        Assert.assertTrue(serverClientManager.getShipper2Cache().get(shipperid).equals("321"));
    }


}
