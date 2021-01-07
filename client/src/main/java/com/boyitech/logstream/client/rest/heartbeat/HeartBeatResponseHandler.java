package com.boyitech.logstream.client.rest.heartbeat;

import com.boyitech.logstream.client.factory.ManagerFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

public class HeartBeatResponseHandler extends SimpleChannelInboundHandler<HttpObject> {

	private StringBuffer contentBuffer = new StringBuffer();

	@Override
	public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {

//		if (msg instanceof HttpResponse) {
//			FullHttpResponse response = (FullHttpResponse) msg;
//
//			if (HttpUtil.isTransferEncodingChunked(response)) {
//				System.out.println("CHUNKED CONTENT {");
//			} else {
//				System.out.println("CONTENT {");
//			}
//		}
		if (msg instanceof HttpContent) {
			HttpContent content = (HttpContent) msg;
			contentBuffer.append(content.content().toString(CharsetUtil.UTF_8));

			if (content instanceof LastHttpContent) {
				ManagerFactory.getClientManager().handleHeartBeatResponse(contentBuffer.toString());
				ctx.close();
			}
//			content.content().release();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
