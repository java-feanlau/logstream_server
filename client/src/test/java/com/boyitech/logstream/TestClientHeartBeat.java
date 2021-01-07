//package com.boyitech.logstream;
//
//import com.boyitech.logstream.client.manager.ClientManager;
//import com.boyitech.logstream.core.setting.ClientSettings;
//import com.boyitech.logstream.core.setting.Settings;
//import com.boyitech.logstream.core.util.FilePathHelper;
//import com.boyitech.logstream.core.util.GsonHelper;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.io.*;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.HashMap;
//import java.util.UUID;
//
///**
// * @author Eric
// * @Title: TestClient
// * @date 2019/4/22 10:56
//
// */
//public class TestClientHeartBeat {
//    ClientManager instance;
//
//    @Before
//    public void prepare() {
//        Settings.load();
//        ClientSettings.load();
//        instance = new ClientManager();
//    }
//
//    //返回404
//    @Test
//    public void testLoseHeartBeat() throws InterruptedException {
//
//        HashMap<String, String> map = new HashMap<>();
//        map.put("http_status", "404");
//        map.put("clientID", "123");
//        String s = GsonHelper.toJson(map);
//
//        for (int i = 1; i <= 10; i++) {
//            instance.handleHeartBeatResponse(s);
//            int loseHeartbeatNumber = instance.getLoseHeartbeatNumber();
//            Assert.assertEquals(i, loseHeartbeatNumber);
//        }
//    }
//
//    //模拟冲突队列重新注册，更改clientID，检查配置中的clientID是否正常被更新
//    @Test
//    public void testUpdateClientID() {
//        String newClientId = UUID.randomUUID().toString();
//
//        HashMap<String, String> map = new HashMap<>();
//        map.put("http_status", "201");
//        map.put("clientID", newClientId);
//        String s = GsonHelper.toJson(map);
//
//        instance.handleHeartBeatResponse(s);
//
//        //读取配置文件设置各全�?变量
//        Path GLOBALSETTINGPATH = Paths.get(FilePathHelper.ROOTPATH, "config", "client.conf");
//        File file = new File(GLOBALSETTINGPATH.toString());
//        Reader reader = null;
//        StringBuffer sb = new StringBuffer();
//        try {
//            reader = new InputStreamReader(new FileInputStream(file));
//            int tempchar;
//            while ((tempchar = reader.read()) != -1) {
//                if (((char) tempchar) != '\r') {
//                    sb.append((char) tempchar);
//                }
//            }
//            reader.close();
//            String[] configs = sb.toString().split("\n");
//            String CLIENTID = configs[0];
//            Assert.assertEquals(CLIENTID, newClientId);
//        } catch (Exception e) {
//            System.exit(0);
//        }
//    }
//
//    //正常心跳
//    @Test
//    public void testNormalHeartBeat() {
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
