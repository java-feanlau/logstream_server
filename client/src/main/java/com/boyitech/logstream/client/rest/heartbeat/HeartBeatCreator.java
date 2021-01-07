package com.boyitech.logstream.client.rest.heartbeat;

import com.boyitech.logstream.client.factory.ManagerFactory;
import com.boyitech.logstream.client.info.Version;
import com.boyitech.logstream.client.setting.HeartBeatSetting;
import com.boyitech.logstream.core.setting.ClientSettings;
import com.boyitech.logstream.core.util.ClientHelper;
import com.boyitech.logstream.core.util.GsonHelper;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class HeartBeatCreator implements Runnable {

	static final Logger LOGGER = LogManager.getLogger("main");
	private SslContext sslCtx;
	private String host;
	private int port;
	private int interval = Integer.parseInt(HeartBeatSetting.heartBeatTime.getValue());

	public HeartBeatCreator(String url) throws URISyntaxException, SSLException {
		URI uri = new URI(url);
		String scheme = "https";
		host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
		port = uri.getPort();
		if (!"https".equalsIgnoreCase(scheme)) {
			System.err.println("Only HTTPS is supported.");
			return;
		}
		// Configure SSL context if necessary.
		final boolean ssl = "https".equalsIgnoreCase(scheme);
		if (ssl) {
			sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		} else {
			sslCtx = null;
		}
	}

	@Override
	public void run() {
		// Configure the client.
		EventLoopGroup group = new NioEventLoopGroup(1);
		try {
			Bootstrap b = new Bootstrap();
			//HeartBeatHandlerInitializer回包
			b.group(group)
					.channel(NioSocketChannel.class)
					.handler(new HeartBeatHandlerInitializer(sslCtx));
			while(true) {
				try {
					Channel ch = b.connect(host, port).sync().channel();
					// 创建完整http请求
					FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, createUrl(),
							Unpooled.wrappedBuffer(heartBeatInfo().getBytes()));
					request.headers().set(HttpHeaderNames.HOST, host);
					request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
					request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
					request.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
					request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
					// 发送请求
					ch.writeAndFlush(request);
					// 等待服务器关闭请求
					ch.closeFuture().sync();
				}catch(Exception e) {
					LOGGER.error("发送心跳失败", e);
				}finally {
					Thread.sleep(interval);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Shut down executor threads to exit.
			group.shutdownGracefully();
		}
	}

	private String heartBeatInfo() {
		String clientStatus = tasksStatus();
		// 构建心跳数据的信息
		Map heartBeatInfo = new HashMap();
		heartBeatInfo.put("fingerPrint", ClientHelper.MACHINECODE);
		heartBeatInfo.put("version", Version.VERSION);
		heartBeatInfo.put("clientStatus", clientStatus);
		String content = GsonHelper.toJson(heartBeatInfo);
		//LOGGER.debug("---发送心跳---：" + content);
		return content;
	}

	private String createUrl() {
		return "/proxies/" + ClientSettings.getClientID() + "/heartbeat";
	}

	private String tasksStatus() {
//		Map tasksMap = new HashMap();
		return ManagerFactory.getClientManager().getClientShipperStatus();
//		return tasksMap;
	}

}
