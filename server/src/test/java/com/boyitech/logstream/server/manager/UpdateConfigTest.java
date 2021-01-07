package com.boyitech.logstream.server.manager;

import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.jdbc.DBUtil;
import com.boyitech.logstream.server.manager.client.ServerClientManager;
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
public class UpdateConfigTest {
    ServerClientManager serverClientManager = null;
    String content = null;
    String shipperConfig = null;

    @Before
    public void befor() {
        HashMap<String, Object> delMap = new HashMap<>();
        delMap.put("client_id", 111);
        try {
            DBUtil.delete("ys_client", delMap);
            DBUtil.delete("ys_client_shipper", delMap);
        } catch (SQLException e) {
            Assert.assertTrue(false);
        }
        serverClientManager = new ServerClientManager();
        //构建配置请求心跳
        Map heartBeatInfo = new HashMap();
        Double casVersion = 1.0;
        heartBeatInfo.put("casVersion", casVersion);
        content = GsonHelper.toJson(heartBeatInfo);

        shipperConfig = "{" +
                "\"moduleType\": \"file\"," +
                "\"index\": \"text_ys\"," +
                "\"readPath\": [\"C:/Users/Eric/Desktop/testShipper\"],"+
                "\"fileNameMatch\": \"testFileShipper.txt\"," +
                "\"threadPollMax\": \"1\"," +
                "\"ignoreOld\": \"false\"," +
                "\"ignoreFileOfTime\": \"86400\"," +
                "\"saveOffsetTime\": \"5\"," +
                "\"encoding\": \"utf8\"," +
                "\"secondOfRead\": \"5\"" +
                "}";
    }


    //没有更新日志
    @Test
    public void testNotNewConfig() {
        //客户端没有
        serverClientManager.registerClient("111",null);
        String result = serverClientManager.handleConfig("111", "1.1.1.1", content);
//        System.out.println(result);
        Map<String, String> stringStringMap = GsonHelper.fromJson(result);
        Assert.assertTrue(stringStringMap.get("http_status") .equals("201") );
    }


    //更新了日志
    @Test
    public void testNewConfig() {
        //客户端没有
        serverClientManager.registerClient("111",null);
        serverClientManager.createClientShipperWorker("111",shipperConfig,"111");
        String result = serverClientManager.handleConfig("111", "1.1.1.1", content);
        Map<String, String> stringStringMap = GsonHelper.fromJson(result);
        Assert.assertTrue(stringStringMap.get("http_status").equals("200") &&  stringStringMap.get("shipperConfigs") !=null);
    }

}

