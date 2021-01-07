package com.boyitech.logstream.core.worker.shipper.netflow.utils;



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
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyManager {

	private static EventLoopGroup bossGroup;
	private static EventLoopGroup workerGroup;
	private static EventLoopGroup singleBossGroup;
	private static EventLoopGroup singleWorkerGroup;
	private static String property = System.getProperty("os.name");

	static {

		if (property.startsWith("Linux")) {
//			bossGroup = new EpollEventLoopGroup();
//			workerGroup = new EpollEventLoopGroup();
//			singleBossGroup = new EpollEventLoopGroup(1);
//			singleWorkerGroup = new EpollEventLoopGroup(1);
			bossGroup = new NioEventLoopGroup();
			workerGroup = new NioEventLoopGroup(5);
			singleBossGroup = new NioEventLoopGroup(1);
			singleWorkerGroup = new NioEventLoopGroup(1);
		} else {
			bossGroup = new NioEventLoopGroup();
			workerGroup = new NioEventLoopGroup(5);
			singleBossGroup = new NioEventLoopGroup(1);
			singleWorkerGroup = new NioEventLoopGroup(1);
		}
	}

	public static ServerBootstrap createTCPServerBootStrap(ChannelHandler handler) {
		ServerBootstrap server = new ServerBootstrap();
		server.group(bossGroup, workerGroup)
				.channel(getTcpServerChannelClass())
				.childHandler(handler);
		return server;
	}

	public static Bootstrap createUDPServerBootStrap(ChannelHandler handler) {
		Bootstrap server = new Bootstrap();
		server.group(workerGroup).channel(getUdpServerChannelClass())
				.handler(handler)
				.option(ChannelOption.SO_RCVBUF, 1024 * 1024)// 设置UDP读缓冲区为1M
                .option(ChannelOption.SO_SNDBUF, 1024 * 1024);// 设置UDP写缓冲区为1M
		return server;
	}

	public static ServerBootstrap createSingleTCPServerBootStrap(ChannelHandler handler) {
		ServerBootstrap server = new ServerBootstrap();
		server.group(singleBossGroup, singleWorkerGroup).channel(getTcpServerChannelClass())
				.childHandler(handler);
		return server;
	}

	public static Bootstrap createSingleUDPServerBootStrap(ChannelHandler handler) {
		Bootstrap server = new Bootstrap();
		server.group(singleWorkerGroup)
				.channel(getUdpServerChannelClass())
				.handler(handler)
				.option(ChannelOption.SO_RCVBUF, 1024 * 1024)// 设置UDP读缓冲区为1M
                .option(ChannelOption.SO_SNDBUF, 1024 * 1024);// 设置UDP写缓冲区为1M
		return server;
	}

	public static Bootstrap createBootStrap(ChannelHandler handler) {
		Bootstrap server = new Bootstrap();
		server.group(workerGroup).channel(NioSocketChannel.class).handler(handler);
		return server;
	}

	public static Class<? extends ServerSocketChannel> getTcpServerChannelClass() {
		if (property.startsWith("Linux")) {
//			return EpollServerSocketChannel.class;
			return NioServerSocketChannel.class;
		}else {
			return NioServerSocketChannel.class;
		}
	}

	public static Class<? extends DatagramChannel> getUdpServerChannelClass() {
		if (property.startsWith("Linux")) {
//			return EpollDatagramChannel.class;
			return NioDatagramChannel.class;
		}else {
			return NioDatagramChannel.class;
		}
	}
}
