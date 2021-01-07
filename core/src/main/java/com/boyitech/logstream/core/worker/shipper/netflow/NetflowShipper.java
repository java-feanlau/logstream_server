package com.boyitech.logstream.core.worker.shipper.netflow;


import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.manager.netty.NettyManager;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.shipper.BaseShipper;
import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;
import com.boyitech.logstream.core.worker.shipper.netflow.info.Flow;
import com.boyitech.logstream.core.worker.shipper.netflow.info.Netflow;
import com.boyitech.logstream.core.worker.shipper.netflow.utils.PacketToNetflowDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.util.ArrayList;
import java.util.List;

public class NetflowShipper extends BaseShipper {

    NetflowShipperConfig configuration;
    public NetflowShipper(BaseShipperConfig config) {
        super(config);
        this.configuration = (NetflowShipperConfig) config;
    }

    public NetflowShipper(String ShipperId,BaseShipperConfig config) {
        super(ShipperId,config);
        this.configuration = (NetflowShipperConfig) config;
    }


    @Override
    public void tearDown() {

    }

    @Override
    public void execute() throws InterruptedException {
        singleUDPServerBootStrap.bind(configuration.getHost(),configuration.getPort()).sync().await();
    }

    Bootstrap singleUDPServerBootStrap = NettyManager.createSingleUDPServerBootStrap(new SimpleChannelInboundHandler() {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            DatagramPacket packet = (DatagramPacket) msg;
            Netflow netflow = PacketToNetflowDecoder.change(packet);

            List<Event> result = new ArrayList<Event>();
            for (Flow normalFlow : netflow.getNormalFlows()) {
                String o = GsonHelper.toJson(normalFlow);
                Event event = new Event();
                event.setMessage(o);
                event.setLogType("netflow");
                event.setIndex(configuration.getIndex());
                event.setSource(packet.recipient().getHostName());
                if(mark!=null)
                    event.setMark(mark);
                if(configuration.isChangeIndex())
                    event.setIndex(configuration.getIndex());
                result.add(event);
            }
//                meter.mark(result.size());
            count.addAndGet(1);
            lv1Cache.put(result);



        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("服务器启动,监听端口："+configuration.getPort());
            super.channelActive(ctx);
        }
    });

}