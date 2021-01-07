package com.boyitech.logstream.core.worker.porter.syslog;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.IPv4Util;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.porter.BasePorter;
import com.boyitech.logstream.core.worker.porter.BasePorterConfig;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class SyslogPorter extends BasePorter {
    private int retryTimes;
    private List<Event> retryList = new ArrayList<Event>();

    private String ip;
    private int port;
    private DatagramSocket ds;
    private SyslogPorterConfig syslogPorterConfig;

    public SyslogPorter(BasePorterConfig config) {
        super(config);
        this.syslogPorterConfig = (SyslogPorterConfig) config;
        ip = syslogPorterConfig.getIp();
        port = Integer.parseInt(syslogPorterConfig.getPort());
}

    public SyslogPorter(String worerId, BasePorterConfig config) {
        super(worerId, config);
        this.syslogPorterConfig = (SyslogPorterConfig) config;
    }

    @Override
    public boolean register() {
        try {
            ds = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void tearDown() {
        ds.close();
    }

    @Override
    public void execute() throws InterruptedException {
        if (lv3Cache.size() != 0) {
            List<Event> eventList = lv3Cache.poll(20000);
            this.forwarding(eventList);
//            meter.mark(eventList.size());
            count.addAndGet(eventList.size());
        }
    }

    private void forwarding(List<Event> list) {
        for (Event e : list) {
//            System.out.println(e.getMessage());
            //todo
//            forwarding(e.getJsonMessage());
            forwarding(e.getMessage());
        }
    }

    private void forwarding(String message) {
        byte[] data = message.getBytes();
        DatagramPacket dp = null;
        try {
            dp = new DatagramPacket(data, data.length,
                    InetAddress.getByAddress(IPv4Util.ipToBytesByInet(ip)), port);
            ds.send(dp);

        } catch (IOException e1) {
            LOGGER.error("转发发生错误", e1);
            this.addException(e1.getMessage());
        }
    }

}
