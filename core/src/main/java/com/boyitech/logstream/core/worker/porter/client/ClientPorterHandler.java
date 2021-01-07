package com.boyitech.logstream.core.worker.porter.client;

import io.netty.channel.*;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClientPorterHandler extends ChannelInboundHandlerAdapter {
	static final Logger LOGGER = LogManager.getLogger("main");
	private final CountDownLatch latch = new CountDownLatch(1);
	private StringBuffer contentBuffer = new StringBuffer();
	private String result;

	//channelActive:通道激活时触发，当客户端connect成功后，服务端就会接收到这个事件，从而可以把客户端的Channel记录下来，供后面复用
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
	//	LOGGER.info("channelActive ctx1:"+ctx);
		if (!isAutoRead(ctx)) {
		//	LOGGER.info("channelActive ctx2:"+ctx);
			ctx.read();
		}
		super.channelActive(ctx);
	}

	//在连接断开时都会触发 channelInactive 方法
	@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (latch.getCount() != 0L) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Connection closed without response: {}", ctx);
			}
		}
		super.channelInactive(ctx);
	}

	//channelRead:当收到对方发来的数据后，就会触发，参数msg就是发来的信息，可以是基础类型，也可以是序列化的复杂对象。
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			//LOGGER.info("in channelRead" + " msg:" + msg + ",ctx:" + ctx);
			if (msg instanceof HttpContent) {
				//LOGGER.info("in channelRead&& msg instanceof HttpContent");
				HttpContent content = (HttpContent) msg;
				//LOGGER.info("content：" + content);
				//LOGGER.info("content.content：" + content.content().toString(CharsetUtil.UTF_8));
				contentBuffer.append(content.content().toString(CharsetUtil.UTF_8));
				if (content instanceof LastHttpContent) {
					result = contentBuffer.toString();
					//LOGGER.info("result:" + result);
					latch.countDown();
					ctx.close();
				}
				//content.content().release();
			}
		}
		catch (Exception e){
			LOGGER.error("ClientPorterHandler.channelRead error! "+e);
		}
		finally {
			ReferenceCountUtil.release(msg);
		}

	}


	public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
		return latch.await(timeout, unit);
	}

	private static boolean isAutoRead(ChannelHandlerContext ctx) {
		Channel channel = ctx.channel();
		ChannelConfig config = channel.config();

		return config.isAutoRead();
	}

	public String response() {
		//LOGGER.info("response:"+result);
		return result;
	}
}
