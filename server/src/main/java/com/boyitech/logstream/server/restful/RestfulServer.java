package com.boyitech.logstream.server.restful;

import com.boyitech.logstream.core.manager.netty.NettyManager;
import com.boyitech.logstream.core.util.os.OSinfo;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

public class RestfulServer {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
	private List<Channel> channelList;
	private ServerBootstrap server;
	private SslContext sslCtx;

	public RestfulServer(String ip, int port, boolean ssl) throws InterruptedException {
        if (ssl) {
            SelfSignedCertificate ssc;
			try {
				ssc = new SelfSignedCertificate();
				sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
			} catch (CertificateException e1) {
				e1.printStackTrace();
			} catch (SSLException e) {
				e.printStackTrace();
			}
        } else {
            sslCtx = null;
        }
        channelList = new ArrayList<>();
        // 初始化服务
        initServer();

        // 添加绑定到指定ip端口的Channel
        Channel ch;
		ch = server.bind(ip, port).channel();
		channelList.add(ch);
	}

	private void initServer() {
		if(OSinfo.getOSname().toString()=="Linux") {
//			bossGroup = new EpollEventLoopGroup();
//			workerGroup = new EpollEventLoopGroup();
			bossGroup = new NioEventLoopGroup();
			workerGroup = new NioEventLoopGroup(5);
			System.out.println("linux多线程");
		}else{
			bossGroup = new NioEventLoopGroup();
			workerGroup = new NioEventLoopGroup(5);
			System.out.println("windows多线程");
		}
		server = new ServerBootstrap();
        server.option(ChannelOption.SO_BACKLOG, 1024);
        server.group(bossGroup, workerGroup)
         .channel(NettyManager.getTcpServerChannelClass())
         .handler(new LoggingHandler(LogLevel.INFO))
         .childHandler(new ServerInitializer(sslCtx));
	}


	public void addChannel(String ip, int port) throws InterruptedException {
		Channel ch;
		ch = server.bind(ip, port).channel();
		channelList.add(ch);
	}

	public void removeChannel(String ip, int port) {
		channelList.get(0).close();
	}

	public void shutdown() {
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}

}
