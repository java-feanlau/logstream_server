package com.boyitech.logstream.server.manager;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.jdbc.DBUtil;
import com.boyitech.logstream.server.factory.SingleManagerFactory;
import com.boyitech.logstream.server.manager.client.ServerClientManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Eric
 * @Title: HeartBeatTest
 * @date 2019/4/19 10:06
 * @Description: TODO
 */
public class HandleBulkTest {
    ServerClientManager serverClientManager = null;
    String content = null;
    String cacheLv1 = null;

    @Before
    public void befor() throws InterruptedException {
        serverClientManager = new ServerClientManager();
        cacheLv1 = new ServerManager().createCacheLv1();
        cacheLv1 = SingleManagerFactory.getServerManager().createCacheLv1();
        HashMap<String, Object> delMap = new HashMap<>();
        delMap.put("client_id", "111");
        try {
            DBUtil.delete("ys_client", delMap);
            DBUtil.delete("ys_client_shipper", delMap);
        } catch (SQLException e) {
            Assert.assertTrue(false);
        }

    }


    //采集引擎没有对应的缓存创建缓存
    @Test
    public void testNotCache() {
        //客户端没有
        serverClientManager.registerClient("111", null);
        String clientShipperWorker = serverClientManager.createClientShipperWorker("111", "{}", "111");
        System.out.println(clientShipperWorker);
        List<Event> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Event event = new Event();
            event.setMessage(i + "");
            event.setMark(clientShipperWorker);
            list.add(event);
        }
        List bulk = new ArrayList();
        for (Event e : list) {
            bulk.add(e.bulkMap());
        }
        content = GsonHelper.toJson(bulk);

        String reslut = serverClientManager.handleBulk("111", "1.1.1.1", content);
        Assert.assertTrue(reslut.contains("400"));
    }


    //写入缓存成功
    @Test
    public void testSuccess() {
        //客户端没有
        serverClientManager.registerClient("111", null);
        String clientShipperWorker = serverClientManager.createClientShipperWorker("111", "{}", cacheLv1);
        System.out.println(clientShipperWorker);
        List<Event> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Event event = new Event();
            event.setMessage(i + "");
            event.setMark(clientShipperWorker);
            list.add(event);
        }
        List bulk = new ArrayList();
        for (Event e : list) {
            bulk.add(e.bulkMap());
        }
        content = GsonHelper.toJson(bulk);

        String reslut = serverClientManager.handleBulk("111", "1.1.1.1", content);
        System.out.println(reslut);
        Assert.assertTrue(reslut.contains("200"));
    }





}

