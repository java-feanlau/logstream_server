package com.boyitech.logstream.server.worker.porter;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.factory.CacheFactory;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.porter.syslog.SyslogPorter;
import com.boyitech.logstream.core.worker.porter.syslog.SyslogPorterConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Eric
 * @Title: SysLogPorterTest
 * @date 2019/4/15 15:23
 * @Description: TODO
 */
public class SysLogPorterTest {
    private String porterConfig;
    private Event event;
    UdpServerSocket udpServerSocket = null;
    String serverHost = "172.17.20.53";
    int serverPort = 514;

    @Before
    public void receive() throws Exception {
        //这里的IP是你本机的IP也就是syslog服务器的IP
        udpServerSocket = new UdpServerSocket(serverHost, serverPort);


    }

    @Test
    public void testSyslog() throws InterruptedException, IOException {
        BaseCache cache = CacheFactory.createCache();
        for (int i = 0; i < 1000; i++) {
            Event event = new Event();
            event.setMessage("219.220.241.137 - - [04/Dec/2018:15:43:52 +0800] \"GET /epstar/web/applications/HRMS/NDKHGL/GRTB/GRZBZP/index.jsp?l=002&current.model.id=5c4o136-qyn2no-g6wz3jau-1-g6wzectu-k HTTP/1.1\" 200 2861\n");
            Map map = new HashMap<>();
            map.put("test", "test");
            map.put("message", event.getMessage());
            event.setFormat(map);
            event.setIndex("test514");
            event.setLogType("nginx_success");
            cache.offer(event);
        }


        porterConfig = "{" +
                "\"moduleType\": \"syslog\"," +
                "\"ip\": \"" + serverHost + "\"," +
                "\"port\": \"" + serverPort + "\"" +
                "}";
        Map<String, String> map1 = GsonHelper.fromJson(porterConfig);
        SyslogPorterConfig config = new SyslogPorterConfig(map1);
        SyslogPorter syslogPorter = new SyslogPorter(config);



        syslogPorter.setLv3Cache(cache);
        syslogPorter.doStart();

//        while (syslogPorter.getCount().get() != 1) {
//
//            Thread.sleep(1000);
//        }
        String receive = udpServerSocket.receive();
        Assert.assertTrue(receive.contains("219.220.241.137"));

    }
}

class UdpServerSocket {
    private byte[] buffer = new byte[1024];

    private DatagramSocket ds = null;

    private DatagramPacket packet = null;

    private InetSocketAddress socketAddress = null;

    private String orgIp;

    /**
     * 构造函数，绑定主机和端口.
     * @param host 主机
     * @param port 端口
     * @throws Exception
     */
    public UdpServerSocket(String host, int port) throws Exception {
        socketAddress = new InetSocketAddress(host, port);
        ds = new DatagramSocket(socketAddress);
        System.out.println("--------------service start----------------");
    }

    /**
     * 接收数据包，该方法会造成线程阻塞.
     * @return 返回接收的数据串信息
     * @throws IOException

    - 下午10:38:24
     */
    public final String receive() throws IOException {
        packet = new DatagramPacket(buffer, buffer.length);
        ds.receive(packet);
        orgIp = packet.getAddress().getHostAddress();
        String info = new String(packet.getData(), 0, packet.getLength());
        System.out.println(info);
        //System.out.println("CONTENT="+info+":SOURCE_IP="+packet.getAddress().getHostAddress()+"SOURCE_PORT:"+packet.getPort());
        return info;
    }


    /**
     * 测试方法.
     * @param args
     * @throws Exception

    - 下午10:49:50
     */
//    public static void main(String[] args) throws Exception {
//        //这里的IP是你本机的IP也就是syslog服务器的IP
//        String serverHost = "172.17.20.53";
//        int serverPort = 514;
//        UdpServerSocket udpServerSocket = new UdpServerSocket(serverHost, serverPort);
//        while (true) {
//            udpServerSocket.receive();
//        }
//    }
}
