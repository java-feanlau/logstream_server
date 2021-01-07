package com.boyitech.logstream.server.test.syslog;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.IPv4Util;
import com.boyitech.logstream.core.worker.porter.BasePorter;
import com.boyitech.logstream.core.worker.porter.BasePorterConfig;

import javax.swing.text.TabExpander;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SyslogPorter extends BasePorter {
    private static int a = 0;
    private int retryTimes;
    private List<Event> retryList = new ArrayList<Event>();

    private String ip;
    private int port;
    private DatagramSocket ds;
    private SyslogPorterConfig syslogPorterConfig;
    public static long aa;

    public SyslogPorter(BasePorterConfig config) {

        super(config);
        syslogPorterConfig = (SyslogPorterConfig) config;
        ip = syslogPorterConfig.getIp();
        port = Integer.parseInt(syslogPorterConfig.getPort());
    }

    public SyslogPorter(String worerId, BasePorterConfig config) {
        super(worerId, config);
        syslogPorterConfig = (SyslogPorterConfig) config;
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
    public void execute() throws IOException, InterruptedException {
//        BufferedReader fileInputStream = new BufferedReader(new FileReader("C:\\Users\\Eric\\Desktop\\apachSuccess.txt")) ;
//        String tempString = null;
//        while ((tempString = fileInputStream.readLine()) != null) {
//            this.forwarding(tempString);
//        }
        while (true){
           // String tempString="<189>date=2019-03-07 time=16:24:05 logver=52 devid=FGT3HD3917800365 devname=Yantai-300D-2 logid=0000000013 type=traffic subtype=forward level=notice vd=root srcip=172.20.20.71 srcport=60057 srcintf=\"port3\" dstip=40.79.85.125 dstport=443 dstintf=\"port2\" poluuid=8e2e0450-24d7-51e7-ec63-7bd1f9f5ba28 sessionid=585230576 proto=6 action=close policyid=25 dstcountry=\"United States\" srccountry=\"Reserved\" trandisp=noop service=HTTPS duration=5 sentbyte=112 rcvdbyte=140 sentpkt=2 rcvdpkt=3\n";

            String tempString="1515";
            this.forwarding(tempString);
            Thread.sleep(0);
         //   System.out.println(aa++);
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

    public static void main(String args[]) throws IOException, InterruptedException {
        HashMap<String, String> map = new HashMap<>();
       // map.put("ip", "172.17.20.54");
        map.put("ip", "172.17.100.100");
        map.put("port", "1515");
        map.put("moduleType", "syslog");
        System.out.print(map);
        BasePorterConfig conf = new SyslogPorterConfig(map);
        SyslogPorter syslogPorter = new SyslogPorter(conf);
        syslogPorter.register();
        syslogPorter.execute();


    }

}