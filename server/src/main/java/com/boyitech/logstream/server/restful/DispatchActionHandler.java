package com.boyitech.logstream.server.restful;

import com.boyitech.logstream.server.manager.client.ServerClientManager;
import com.mchange.v2.sql.filter.FilterCallableStatement;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.logging.Logger;

public class DispatchActionHandler extends ChannelInboundHandlerAdapter {
    protected static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger("main");

    private static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");
    private static final AsciiString CONTENT_LENGTH = new AsciiString("Content-Length");
    private static final AsciiString CONNECTION = new AsciiString("Connection");
    private static final AsciiString KEEP_ALIVE = new AsciiString("keep-alive");

    private ServerClientManager clientManager;

    public DispatchActionHandler(ServerClientManager clientManager) {
        this.clientManager = clientManager;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof FullHttpRequest) {

                FullHttpRequest req = (FullHttpRequest) msg;
                // 根据请求的method和uri分发到对应的处理类
                String[] uris = req.uri().split("/");
                String responseBody = "";
                boolean keepAlive = HttpUtil.isKeepAlive(req);
                if (uris.length != 4) {
                    responseBody = "Error in URI format\r\n";
                    LOGGER.warn("Error in URI format");
                } else {
                    InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
                    InetAddress inetaddress = socketAddress.getAddress();
                    String ipAddress = inetaddress.getHostAddress();
                    try {
                        switch (uris[1]) {
                            case "proxies":
                                switch (uris[3]) {
                                    case "heartbeat":
                                        responseBody = clientManager.handleHeartBeat(
                                                uris[2], ipAddress, req.content().toString(Charset.defaultCharset()));
                                        break;
                                    case "bulk":
                                        responseBody = clientManager.handleBulk(
                                                uris[2], ipAddress, req.content().toString(Charset.defaultCharset()));
                                        break;
                                    case "updateConfig":
                                        responseBody = clientManager.handleConfig(
                                                uris[2], ipAddress, req.content().toString(Charset.defaultCharset()));
                                        break;
                                    default:
                                        responseBody = "{\"http_status\": \"404\",\"message\":\"Unknown action\"}\r\n";
                                }
                                break;
                            default:
                                responseBody = "{\"http_status\": \"404\",\"message\":\"Only Proxies is supported\"}\r\n";
                        }
                    } catch (Exception e) {
                        LOGGER.error("error at DispatchActionHandler" + e);
                        responseBody = "{\"http_status\": \"500\",\"message\":\"error in server\"}\r\n";

                    }
                    if (responseBody == null) {
                        responseBody = "{\"http_status\": \"404\",\"message\":\"Unknown client\"}\r\n";
                    }
                    //req.content().release();
                    //response 收到数据后响应
                }
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK, Unpooled.wrappedBuffer(responseBody.getBytes()));
                response.headers().set(CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
                response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

                if (!keepAlive) {
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    response.headers().set(CONNECTION, KEEP_ALIVE);
                    ctx.write(response);
                }


            } else {
                // ReferenceCountUtil.release(msg);
                //System.err.print("error msg");
                LOGGER.warn("error at DispatchActionHandler,msg::" + msg + " ,msg.Class:" + msg.getClass());
            }
        } catch (Exception e) {
            LOGGER.error("error at DispatchActionHandler:" + e);
        } finally {
            ReferenceCountUtil.release(msg);
        }
        //ctx.close();

    }

    /**
     * 响应HTTP的请求
     *
     * @param ctx
     * @param req
     * @param jsonStr
     */
    private void ResponseJson(ChannelHandlerContext ctx, FullHttpRequest req, String jsonStr) {
        boolean keepAlive = HttpUtil.isKeepAlive(req);
        byte[] jsonByteByte = jsonStr.getBytes();
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK, Unpooled.wrappedBuffer(jsonByteByte));
        response.headers().set(CONTENT_TYPE, "text/json");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

        if (!keepAlive) {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, KEEP_ALIVE);
            ctx.write(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.info("cause：" + cause.toString());
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 获取请求的内容
     *
     * @param request
     * @return
     */
    private String parseJosnRequest(FullHttpRequest request) {
        ByteBuf jsonBuf = request.content();
        String jsonStr = jsonBuf.toString(CharsetUtil.UTF_8);
        jsonBuf.release();
        return jsonStr;
    }

    private void proxies(FullHttpRequest request) {

    }

}
