package com.boyitech.logstream.core.worker.porter.client;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.manager.netty.NettyManager;
import com.boyitech.logstream.core.setting.ClientSettings;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.porter.BasePorter;
import com.boyitech.logstream.core.worker.porter.BasePorterConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientPorter extends BasePorter {

	private SslContext sslCtx;
	private String host;
	private int port;

	public ClientPorter(BasePorterConfig config) {
		super(config);
		String addr = ClientSettings.getServerAddr();
		URI uri;
		try {
			uri = new URI(addr);
			String scheme = "https";
			host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
			port = uri.getPort();
		} catch (URISyntaxException e1) {
			LOGGER.fatal("解析服务端地址错误，系统退出", e1);
			System.exit(1);
		}
		try {
			sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		} catch (SSLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ClientPorter(String worerId, BasePorterConfig config) {
		super(worerId, config);
		String addr = ClientSettings.getServerAddr();
		URI uri;
		try {
			uri = new URI(addr);
			String scheme = "https";
			host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
			port = uri.getPort();
		} catch (URISyntaxException e1) {
			LOGGER.fatal("解析服务端地址错误，系统退出", e1);
			System.exit(1);
		}
		try {
			sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		} catch (SSLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean register() {
		return true;
	}

	@Override
	public void execute() throws InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		List<Event> retryList = new ArrayList();

		while (runSignal) {
			try {

				execute(retryList);
			} catch (InterruptedException e) {
				LOGGER.warn(Thread.currentThread().getName() + "被异常中断");
				this.notify();
				break;
			}
		}
		tearDown();
		if (countDownLatch != null) {
			countDownLatch.countDown();
			LOGGER.info(Thread.currentThread().getName() + "退出");
		}
	}

	public void execute(List<Event> retryList) throws InterruptedException {
//		LOGGER.debug("cache:"+lv2Cache.size());
//		LOGGER.debug("retry:"+retryList.size());
		List<Event> eventList = (List<Event>) lv2Cache.poll(1000 - retryList.size());
		eventList.addAll(retryList);
		retryList.clear();
		//缓存为空，此时读的数据为空
		if (eventList.size() < 1) {
			Thread.sleep(1000);
			return;//????
		}

//		LOGGER.debug("list:"+eventList.size());
		ClientPorterHandler handler = new ClientPorterHandler();
		//LOGGER.info("sslCtx:"+sslCtx.toString());
		Bootstrap server = NettyManager.createBootStrap(new ClientPorterHandlerInitializer(sslCtx, handler));
		Channel ch = server.connect(host, 6666).sync().channel();
		String response;
		try {
			ChannelFuture f = ch.read().closeFuture();
			while (!f.isSuccess()) {
				// 创建完整http请求
				FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
						HttpMethod.POST, createUrl(),
						Unpooled.wrappedBuffer(bulk(eventList).getBytes(StandardCharsets.UTF_8)));
				request.headers().set(HttpHeaderNames.HOST, host);
				request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
				request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
				request.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
				request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());

				// 发送请求
				ch.writeAndFlush(request).await();
				f.sync();
			}
		} finally {
			response = handler.response();
			// 关闭连接
			ch.close();
		}
		// 根据response检查bulk中每条数据是否成功接收
        List<Map> res = new ArrayList<Map>();
        try {
			 res = GsonHelper.formJson(response);
		}
		catch (Exception e){
            LOGGER.warn("response是否有数据？ "+"response:"+response+" ,res:"+res);
		}
		if (res != null&&res.size()>0) {
//        	LOGGER.error("res_if:"+res);
			for (int i=0;i<res.size()-1;i++) {
				switch (res.get(i).get("status").toString()) {
					case "503":
						retryList.add(eventList.get(i));
				}
			}
		}else {
//        	LOGGER.error("res_else:"+res);
        	LOGGER.error("HTTP报文"+response);
			retryList.addAll(eventList);
			this.addException("HTTP报文长度超过大小");
			LOGGER.error("HTTP报文长度超过大小");
			throw new InterruptedException("连接断开！");
		}

//		meter.mark(eventList.size()-retryList.size());
		count.addAndGet(eventList.size()-retryList.size());
	}

	@Override
	public void tearDown() {
		// 等待服务器关闭请求
		// ch.closeFuture().sync();

	}

	private String bulk(List<Event> list) {
		List bulk = new ArrayList();
		for (Event e : list) {
			//LOGGER.info(e.bulkMap().toString());
			//LOGGER.info(e.bulkMap().get("inetInfo"));
			bulk.add(e.bulkMap());
		}
		String result = GsonHelper.toJson(bulk);
		// LOGGER.debug("构造bulk:\n" + result);
		return result;
	}

	private String createUrl() {
		return "/proxies/" + ClientSettings.getClientID() + "/bulk";
	}

}
