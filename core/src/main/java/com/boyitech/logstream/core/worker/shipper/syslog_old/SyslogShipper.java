package com.boyitech.logstream.core.worker.shipper.syslog_old;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.worker.shipper.BaseShipper;
import com.boyitech.logstream.core.worker.BaseWorker;
import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright 2015 HP
 * All right reserved.
 * UDP服务类.  采集syslog
 *
 * @version 1.0
 * Creation date: 2015-8-18 - 下午16:32:31
 */
public class SyslogShipper extends BaseShipper {
    private byte[] buffer = new byte[1024];
    private DatagramSocket ds = null;
    private SyslogShipperConfig config;
    // 分配索引的规则
//    private FilterRuler ruler;


    /**
     * 构造函数，绑定主机和端口.
     *
     * @throws Exception
     */
    public SyslogShipper(BaseShipperConfig config) {
        super(config);
        this.config = (SyslogShipperConfig) config;
//        ruler = new FilterRuler(this.config.getFilterRuleList());
    }

    public SyslogShipper(String shipperID, BaseShipperConfig config) {
        super(shipperID, config);
        this.config = (SyslogShipperConfig) config;
    }

    @Override
    public boolean register() {
        InetSocketAddress socketAddress = new InetSocketAddress(config.getHost(), config.getPort());
        System.out.println(config.getHost());
        try {
            ds = new DatagramSocket(socketAddress);
        } catch (SocketException e) {
            BaseWorker.LOGGER.error("申请资源监听端口失败：" + e);
            e.printStackTrace();
            this.addException("申请资源监听端口失败：" + e.getMessage());
            return false;
        }
        System.out.println("--------------service start----------------");
        return true;
    }

    @Override
    public void tearDown() {
        try {
            if (ds == null) {
                return;
            } else {
                ds.close();
            }
        } catch (Exception ex) {
            BaseWorker.LOGGER.error("释放资源失败：" + ex.getMessage());
            this.addException("释放资源失败：" + ex.getMessage());
        }
    }

    @Override
    public void execute() throws InterruptedException {
        if (ds == null || ds.isClosed())
            return;
        receive();
    }

    /**
     * 设置超时时间，该方法必须在bind方法之后使用.
     *
     * @param timeout 超时时间
     * @throws Exception
     */
    public final void setSoTimeout(int timeout) throws Exception {
        ds.setSoTimeout(timeout);
    }

    /**
     * 获得超时时间.
     *
     * @return 返回超时时间.
     * @throws Exception - 下午10:34:36
     */
    public final int getSoTimeout() throws Exception {
        return ds.getSoTimeout();
    }


    /**
     * 接收数据包，该方法会造成线程阻塞.
     *
     * @return 返回接收的数据串信息
     * @throws IOException - 下午10:38:24
     */
    public void receive() throws InterruptedException {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            ds.receive(packet);

        } catch (IOException e) {
            BaseWorker.LOGGER.error("接受syslog数据包发生异常：" + e.getMessage());
            this.addException("接受syslog数据包发生异常：" + e.getMessage());
        }

        String srcIp = packet.getAddress().getHostAddress();
        int srcPort = packet.getPort();
//        Event e = ruler.findIndexForEvent(info);
//        if(e == null){
//            return;
//        }
        Event e = new Event();
        String message = new String(packet.getData(), 0, packet.getLength());
        e.setMessage(message);
        e.setSource(srcIp);
//        System.out.println(e);
        lv1Cache.put(e);
        count.addAndGet(1);
    }


    /**
     * 测试方法.
     *
     * @param args
     * @throws Exception - 下午10:49:50
     */
    public static void main(String[] args) throws Exception {
        //这里的IP是你本机的IP也就是syslog服务器的IP
        Map map = new HashMap();
//        List<Map<String, String>> list = new ArrayList<>();
//        Map<String, String> filterMap = new HashMap<>();
//        filterMap.put("index", "aaa");
//        filterMap.put("srcAddr", "172.17.20.0~172.17.20.48");
//        filterMap.put("lv1Cache", "123");
//        list.add(filterMap);
        map.put("moduleType", "syslog");
        map.put("index", "syslog");
        map.put("port", "8888");
        map.put("host", "172.17.20.48");
        map.put("protocol", "udp");
//        map.put("filters", list);

        SyslogShipperConfig syslogShipperConfig = new SyslogShipperConfig(map);
        SyslogShipper syslogShipper = new SyslogShipper(syslogShipperConfig);
        syslogShipper.doStart();

    }


    //设置报文的缓冲长度.
//    public final void setLength(int bufsize) {
//        packet.setLength(bufsize);
//    }

    //获得发送回应的IP地址
//    public final InetAddress getResponseAddress() {
//        return packet.getAddress();
//    }

    //获得回应的主机的端口.
//    public final int getResponsePort() {
//        return packet.getPort();
//    }

}