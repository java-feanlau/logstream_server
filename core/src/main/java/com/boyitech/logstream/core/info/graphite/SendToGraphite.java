package com.boyitech.logstream.core.info.graphite;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * @author Eric
 * @Title: SendToGraphite
 * @date 2019/3/20 10:48
 * @Description: TODO
 */
public class SendToGraphite {

    public static void main(String[] args) throws IOException {


        while (true){
            long l = System.currentTimeMillis() ;

            String msg = "java.logstream.test2 6860 "+l/1000;
            try {
                DatagramSocket client = new DatagramSocket();
                byte[] sendBuf = new byte[1024];
                sendBuf = msg.getBytes("utf-8");
                DatagramPacket sendPacket = new DatagramPacket(sendBuf, 0, sendBuf.length, InetAddress.getByName("172.17.30.10"), 2003);
                client.send(sendPacket);
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


//
//        Socket conn = new Socket("172.17.30.10", 2003);
//        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
//        dos.writeBytes("YOUR-API-KEY.foo 1.2\n");
//        conn.close();


//        Meter test = MetricHelper.createMeter("test");
//        test.mark(100);
//
//        String host = MetricsSettings.GRAPHITEHOST.getValue();
//        int port = MetricsSettings.GRAPHITEPORT.getValue();
//        final Graphite graphite = new Graphite(new InetSocketAddress(host, port));
//        final GraphiteReporter reporter = GraphiteReporter.forRegistry(MetricHelper.getInstance())
//                .prefixedWith("java.logStream")
//                .convertRatesTo(TimeUnit.SECONDS)
//                .convertDurationsTo(TimeUnit.MILLISECONDS)
//                .filter(MetricFilter.ALL)
//                .build(graphite);
//        int seconds = MetricsSettings.GRAPHITEINTERVAL.getValue();
//        reporter.start(1, TimeUnit.MILLISECONDS);
    }

}
