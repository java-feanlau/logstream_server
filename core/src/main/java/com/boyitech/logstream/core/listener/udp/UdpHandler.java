package com.boyitech.logstream.core.listener.udp;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.info.InetInfo;
import com.boyitech.logstream.core.util.filter_rule.FilterRule;
import com.boyitech.logstream.core.worker.shipper.BaseListenerShipper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
 * @Author Eric Zheng
 * @Description 处理udp的处理器
 * @Date 17:23 2019/8/1

 **/
public class UdpHandler extends ChannelInitializer<NioDatagramChannel> {
    protected static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger("main");

    private ConcurrentHashMap<FilterRule, ArrayList<BaseListenerShipper>> register;

    public UdpHandler(ConcurrentHashMap<FilterRule, ArrayList<BaseListenerShipper>> register) {
        this.register = register;
        LOGGER.info("start udp handler");
    }

    @Override
    protected void initChannel(NioDatagramChannel ch) throws NullPointerException{
        ch.pipeline().addLast(new SimpleChannelInboundHandler<DatagramPacket>() {

            @Override
            protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
                try {
                    InetSocketAddress sender = msg.sender();
                    InetSocketAddress reveiver = msg.recipient();
                    String srcAddr = sender.getAddress().getHostAddress();
                    String dstAddr = reveiver.getAddress().getHostAddress();
                    int toPort = msg.recipient().getPort();
                    int port = sender.getPort();
                    InetInfo inetInfo = new InetInfo(srcAddr, port, "null", toPort, "udp");
                    //???????
                    // inetInfo.setSrcAddr(dstAddr); //因为dstAddr测试下来是0.0.0.0，所以暂时就塞进去了，要不然如果配置目的ip（例如：172.17.100.100）可能会导致所以数据接受不了
                    //inetInfo.setDstPort(toPort);
                    Set<Map.Entry<FilterRule, ArrayList<BaseListenerShipper>>> entries = register.entrySet();
                    Iterator<Map.Entry<FilterRule, ArrayList<BaseListenerShipper>>> iterator = entries.iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<FilterRule, ArrayList<BaseListenerShipper>> next = iterator.next();
                        FilterRule filterRule = next.getKey();
                        if (filterRule.contains(inetInfo)) {
                            ByteBuf buf = msg.content();
                            String message = buf.toString(CharsetUtil.UTF_8);
                            Event event = new Event();
                            event.setMessage(message);
                            event.setSource(srcAddr);
                            //event.setInetInfo(inetInfo);
                            ArrayList<BaseListenerShipper> value = next.getValue();
                            for (BaseListenerShipper baseListenerShipper : value) {
                                if (baseListenerShipper.getPort() == toPort) {
                                    baseListenerShipper.passMessage(event);
                                }
                            }
                            //buf.release();
                        }
                    }
                }
                catch (Exception e){
                    LOGGER.error("Error at UDPHandler:"+e);
                }
                finally {
                    ReferenceCountUtil.release(msg);
                }
               // msg.content().release();
            }
        });//3.4在pipeline中加入解码器
    }
}