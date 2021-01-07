package com.boyitech.logstream.server.soap;

import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.jdbc.DBUtil;
import com.boyitech.logstream.server.BaseTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Eric
 * @Title: CreateShipper
 * @date 2019/4/9 17:11
 * @Description: Indexer的创建，启动，暂停，销毁soap接口测试
 */

public class ClientTest extends BaseTest {
    private String clientID;
    private String cacheLv1;
    private String indexerConfig;



    //创建，查询，删除客户端
    @Test
    public void testCreateAndDeleteClient() {
        //创建
        Assert.assertTrue(instance.registerClient(clientID,null));
        String registeredClients = instance.getRegisteredClients();
        Map<String, String> stringStringMap = GsonHelper.fromJson(registeredClients);
        Assert.assertTrue(stringStringMap.containsKey(clientID));

        Assert.assertTrue(instance.deleteClient(clientID));
    }

    //ClientShipper的创建，修改，删除，以及状态查询
    @Test
    public void testClientShipper() {
        //创建
        Assert.assertTrue(instance.registerClient(clientID,null));
        //创建客户端shipper
        String clientShipperWorker = instance.createClientShipperWorker(clientID, "{}", cacheLv1);
        Map<String, String> createResult = GsonHelper.fromJson(clientShipperWorker);
        String createSoap = createResult.get("soap_status");
        String shipperID = createResult.get("clientShipperWorkerID");
        Assert.assertEquals("200",createSoap);
        //修改客户端shipper
        String updateResult = instance.updateClientShipperWorker(clientID, shipperID, "{}", cacheLv1);
        Map<String, String> updateSoap = GsonHelper.fromJson(updateResult);
        String updateStatus = updateSoap.get("soap_status");
        Assert.assertEquals("200",updateStatus);
        //删除客户端shipper
        String deleteResult = instance.deleteClientShipperWorker(clientID, shipperID);
        Map<String, String> deleteSoap = GsonHelper.fromJson(deleteResult);
        String deleteStatus = deleteSoap.get("soap_status");
        Assert.assertEquals("200",deleteStatus);


    }


    @Before
    public void testCreateCache() {
        clientID = "123";
        cacheLv1 = instance.createCacheLv1();
        HashMap<String, Object> delMap = new HashMap<>();
        delMap.put("client_id", clientID);
        try {
            DBUtil.delete("ys_client", delMap);
            DBUtil.delete("ys_client_shipper", delMap);
        } catch (SQLException e) {
            Assert.assertTrue(false);
        }
    }


}
