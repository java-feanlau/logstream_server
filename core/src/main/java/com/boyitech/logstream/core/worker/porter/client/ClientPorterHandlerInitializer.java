package com.boyitech.logstream.core.worker.porter.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslContext;

public class ClientPorterHandlerInitializer extends ChannelInitializer<SocketChannel> {

	private final SslContext sslCtx;
	private ClientPorterHandler handler;

	public ClientPorterHandlerInitializer(SslContext sslCtx, ClientPorterHandler handler) {
		this.sslCtx = sslCtx;
		this.handler = handler;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();

		// Enable HTTPS if necessary.
		if (sslCtx != null) {
			p.addLast(sslCtx.newHandler(ch.alloc()));
		}

		p.addLast(new HttpClientCodec());

		// Remove the following line if you don't want automatic content decompression.
		p.addLast(new HttpContentDecompressor());

		// Uncomment the following line if you don't want to handle HttpContents.
		p.addLast(new HttpObjectAggregator(1048576));

		//p.addLast(new ClientPorterHandler());
		p.addLast(handler);
	}

}
