package com.kwp.chat.websocket.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket认证处理器 - 在HTTP握手阶段从URL查询参数中提取token
 *
 * 解决问题：当WebSocket URL包含查询参数（如 /ws?token=xxx）时，
 * WebSocketServerProtocolHandler可能无法正确完成握手，
 * 导致"Connection closed before receiving a handshake response"错误。
 */
@Slf4j
public class WebSocketAuthHandler extends ChannelInboundHandlerAdapter {

    public static final String WEBSOCKET_PATH = "/ws";

    /**
     * 通道属性Key，用于存储从URL查询参数中提取的token
     */
    public static final AttributeKey<String> TOKEN_ATTR = AttributeKey.valueOf("ws.token");

    /**
     * 通道属性Key，用于存储从URL查询参数中提取的clientType
     */
    public static final AttributeKey<String> CLIENT_TYPE_ATTR = AttributeKey.valueOf("ws.clientType");

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof FullHttpRequest)) {
            ctx.fireChannelRead(msg);
            return;
        }

        FullHttpRequest request = (FullHttpRequest) msg;
        String uri = request.uri();
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        String path = decoder.path();

        try {
            if (path.startsWith(WEBSOCKET_PATH)) {
                // 处理OPTIONS预检请求（CORS）
                if (HttpMethod.OPTIONS.equals(request.method())) {
                    log.debug("收到OPTIONS预检请求: {}", uri);
                    sendCorsResponse(ctx, request, HttpResponseStatus.OK);
                    return;
                }

                // 只处理WebSocket升级请求
                if (!isWebSocketUpgrade(request)) {
                    log.warn("非WebSocket升级请求: {}", uri);
                    sendBadRequest(ctx, request);
                    return;
                }

                // 从查询参数中提取token
                String token = getQueryParam(decoder, "token");
                if (token != null && !token.isEmpty()) {
                    ctx.channel().attr(TOKEN_ATTR).set(token);
                    log.debug("从URL提取到token: {}...", token.substring(0, Math.min(20, token.length())));
                }

                // 提取clientType
                String clientType = getQueryParam(decoder, "clientType");
                if (clientType != null && !clientType.isEmpty()) {
                    ctx.channel().attr(CLIENT_TYPE_ATTR).set(clientType);
                }

                // 移除查询参数，避免影响WebSocket握手
                // 将 /ws?token=xxx 改为 /ws
                if (uri.contains("?")) {
                    request.setUri(path);
                }

                // 添加CORS响应头到握手请求
                request.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                request.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "*");

                // 传递给下一个handler（WebSocketServerProtocolHandler）处理握手
                ctx.fireChannelRead(request);
            } else {
                // 不是我们的路径，传递给下一个handler
                ctx.fireChannelRead(request);
            }
        } catch (Exception e) {
            log.error("WebSocket认证处理异常: {}", e.getMessage(), e);
            ReferenceCountUtil.release(request);
            ctx.close();
        }
    }

    /**
     * 判断是否是WebSocket升级请求
     */
    private boolean isWebSocketUpgrade(FullHttpRequest request) {
        String upgrade = request.headers().get("Upgrade");
        return upgrade != null && "websocket".equalsIgnoreCase(upgrade);
    }

    /**
     * 获取查询参数
     */
    private String getQueryParam(QueryStringDecoder decoder, String name) {
        var params = decoder.parameters().get(name);
        if (params != null && !params.isEmpty()) {
            return params.get(0);
        }
        return null;
    }

    /**
     * 发送CORS响应
     */
    private void sendCorsResponse(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE, "3600");
        ctx.writeAndFlush(response).addListener(future -> ctx.close());
    }

    /**
     * 发送400错误响应
     */
    private void sendBadRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        ctx.writeAndFlush(response).addListener(future -> ctx.close());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("WebSocket认证处理异常: {}", cause.getMessage(), cause);
        ctx.close();
    }
}
