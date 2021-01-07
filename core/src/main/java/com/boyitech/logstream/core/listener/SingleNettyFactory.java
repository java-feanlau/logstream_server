package com.boyitech.logstream.core.listener;

import com.boyitech.logstream.core.listener.tcp.TcpHandler;
import com.boyitech.logstream.core.listener.udp.UdpHandler;
import com.boyitech.logstream.core.util.filter_rule.FilterRule;
import com.boyitech.logstream.core.util.os.OSinfo;
import com.boyitech.logstream.core.worker.shipper.BaseListenerShipper;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class SingleNettyFactory {

    private static EventLoopGroup bossGroup;
    private static EventLoopGroup workerGroup;



    static {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
    }

    public static ServerBootstrap createTCPServerBootStrap(ChannelHandler handler) {
        ServerBootstrap tcpServerBootStrap = new ServerBootstrap();
        tcpServerBootStrap.group(bossGroup, workerGroup)
                .channel(getTcpServerChannelClass())
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(handler);
        return tcpServerBootStrap;
    }

    public static Bootstrap createUDPServerBootStrap(ChannelHandler handler) {
        Bootstrap udpServerBootStrap = new Bootstrap();
        udpServerBootStrap.group(workerGroup).channel(getUdpServerChannelClass())
                .option(ChannelOption.SO_RCVBUF, 1024 * 1024)// 设置UDP读缓冲区为1M
                .option(ChannelOption.SO_SNDBUF, 1024 * 1024)// 设置UDP写缓冲区为1M
                .handler(handler);
        return udpServerBootStrap;
    }


    public static Class<? extends ServerSocketChannel> getTcpServerChannelClass() {
        if (OSinfo.getOSname().toString() == "Linux") {
//			return EpollServerSocketChannel.class;
            return NioServerSocketChannel.class;
        } else {
            return NioServerSocketChannel.class;
        }
    }

    public static Class<? extends DatagramChannel> getUdpServerChannelClass() {
        if (OSinfo.getOSname().toString() == "Linux") {
//			return EpollDatagramChannel.class;
            return NioDatagramChannel.class;
        } else {
            return NioDatagramChannel.class;
        }
    }
}
