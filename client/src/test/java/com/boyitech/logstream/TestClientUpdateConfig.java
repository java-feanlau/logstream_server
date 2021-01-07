//package com.boyitech.logstream;
//
//import com.boyitech.logstream.client.manager.ClientManager;
//import com.boyitech.logstream.core.setting.ClientSettings;
//import com.boyitech.logstream.core.setting.Settings;
//import com.boyitech.logstream.core.util.GsonHelper;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.HashMap;
//import java.util.UUID;
//
///**
// * @author Eric
// * @Title: TestClient
// * @date 2019/4/22 10:56
//
// */
//public class TestClientUpdateConfig {
//    ClientManager instance;
//
//    @Before
//    public void prepare() {
//        Settings.load();
//        ClientSettings.load();
//        instance = new ClientManager();
//    }
//
//
//    @Test
//    public void testNormalUpdateConfig() {
//        String newClientId = UUID.randomUUID().toString();
//
//        HashMap<String, String> map = new HashMap<>();
//        map.put("http_status", "200");
//        map.put("clientID", newClientId);
//        String s = GsonHelper.toJson(map);
//
//        instance.handleHeartBeatResponse(s);
//        instance.handleHeartBeatResponse(s);
//        Assert.assertEquals(2,instance.getHeartBeatNumer());
//    }
//}
//
