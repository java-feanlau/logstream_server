package com.boyitech.logstream.core.listener.tcp;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.info.InetInfo;
import com.boyitech.logstream.core.util.filter_rule.FilterRule;
import com.boyitech.logstream.core.util.filter_rule.MultilineStateMachine;
import com.boyitech.logstream.core.worker.shipper.BaseListenerShipper;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;

import java.io.UnsupportedEncodingException;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Eric
 * @Title: TcpHandler
 * @date 2019/8/6 9:21
 * @Description: TODO
 */
public class TcpHandler extends ChannelInitializer<SocketChannel> {
    protected ConcurrentHashMap<FilterRule, ArrayList<BaseListenerShipper>> register;

    protected static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger("main");

    public TcpHandler(ConcurrentHashMap<FilterRule, ArrayList<BaseListenerShipper>> register) {
        this.register = register;
        LOGGER.info("start tcp handler");

    }

    @Override
    protected void initChannel(SocketChannel ch) {
//        ch.pipeline().addLast(new RuleBasedIpFilter(new IpFilterRuleHandler()));
        ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
        ch.pipeline().addLast("encoder", new StringEncoder());
        ch.pipeline().addLast("decoder", new ChannelInboundHandlerAdapter()
        {
            private Map<FilterRule, ArrayList<BaseListenerShipper>> oldregister ;
            private ArrayList<BaseListenerShipper> baseListenerShippers = new ArrayList<>();
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException,NullPointerException {
                try {
                    ByteBuf buf = (ByteBuf) msg;
                    byte[] req = new byte[buf.readableBytes()];
                    buf.readBytes(req);
                    String line = new String(req, "UTF-8");

                    if (oldregister == null | oldregister != register) {
                        this.oldregister = new HashMap<>(register);
                        baseListenerShippers.clear();
                        getThisChannelShippers(ctx, line);
                    }
                }
                catch (Exception e){
                    LOGGER.error("Error at TCPHandler:"+e);
                }
                finally {
                    ReferenceCountUtil.release(msg);
                }

            }

            public void getThisChannelShippers(ChannelHandlerContext ctx,String line) throws NullPointerException{
                InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
                InetSocketAddress localAddress = (InetSocketAddress)ctx.channel().localAddress();
                String ip = socketAddress.getAddress().getHostAddress();
                int port = socketAddress.getPort();
                int toPort = localAddress.getPort();
                InetInfo inetInfo = new InetInfo();
                inetInfo.setSrcAddr(ip);
                inetInfo.setSrcPort(port);
                inetInfo.setDstPort(toPort);
                inetInfo.setProtocol("tcp");
                Set<Map.Entry<FilterRule, ArrayList<BaseListenerShipper>>> entries = register.entrySet();
                Iterator<Map.Entry<FilterRule, ArrayList<BaseListenerShipper>>> iterator = entries.iterator();
                while (iterator.hasNext()) {
                    Map.Entry<FilterRule, ArrayList<BaseListenerShipper>> next = iterator.next();
                    FilterRule filterRule = next.getKey();
                    //过滤
                    if (filterRule.contains(inetInfo)) {
                        ArrayList<BaseListenerShipper> value = next.getValue();
                        for (BaseListenerShipper baseListenerShipper : value) {
                            if(baseListenerShipper.getPort() == toPort){
                                dispatcher(ctx, line, inetInfo, baseListenerShipper);
                            }
                        }
                    }
                }
            }

            private void dispatcher(ChannelHandlerContext ctx, String line, InetInfo inetInfo, BaseListenerShipper baseListenerShipper) {
                if (baseListenerShipper.isMultiline()) {
                    MultilineStateMachine msm = new MultilineStateMachine(baseListenerShipper.getMultilineConfig());
//                        MultilineStateMachine msm = shipper2Multiline.get(baseListenerShipper);
                    if (msm.in(line)) {
                        String out = msm.out();
                        Event event = new Event();
                        InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
                        event.setMessage(out);
                        event.setSource(inetSocketAddress.getAddress().getHostAddress());
                    //    event.setInetInfo(inetInfo);
                        baseListenerShipper.passMessage(event);
                    }
                }else {
                    Event event = new Event();
                    InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
                    event.setMessage(line);
                   // event.setInetInfo(inetInfo);
                    event.setSource(inetSocketAddress.getAddress().getHostAddress());
                    baseListenerShipper.passMessage(event);

                }
            }

//            public void getShipperMultiline(){
//                Set<BaseListenerShipper> ListenerShippers = shipper2Multiline.keySet();
//                Iterator<BaseListenerShipper> iterator = ListenerShippers.iterator();
//                while (iterator.hasNext()) {
//                    if (!ListenerShippers.contains(iterator.next())) {
//                        iterator.remove();
//                    }
//                }
//                for (BaseListenerShipper baseListenerShipper : baseListenerShippers) {
//                    if (baseListenerShipper.isMultiline()) {
//                        if(!shipper2Multiline.containsKey(baseListenerShipper)){
//                            shipper2Multiline.put(baseListenerShipper
//                                    , new MultilineStateMachine(baseListenerShipper.getMultilineConfig()));
//                        }
//                    } else {
//                        shipper2Multiline.put(baseListenerShipper, null);
//                    }
//                }
//
//
//
//            }
        });
    }

}
