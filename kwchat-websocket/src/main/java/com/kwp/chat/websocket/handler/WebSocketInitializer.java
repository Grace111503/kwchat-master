package com.kwp.chat.websocket.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * WebSocket通道初始化器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketInitializer extends ChannelInitializer<SocketChannel> {

    private final WebSocketHandler webSocketHandler;
    private final HeartbeatHandler heartbeatHandler;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // HTTP编解码器
        pipeline.addLast(new HttpServerCodec());

        // HTTP消息聚合器
        pipeline.addLast(new HttpObjectAggregator(65536));

        // WebSocket认证处理器 - 从URL查询参数中提取token，解决带参数URL握手失败问题
        pipeline.addLast(new WebSocketAuthHandler());

        // WebSocket协议处理器
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws", "", false, 65536, false, true));

        // 空闲状态检测（读空闲60秒，写空闲30秒，全部空闲90秒）
        pipeline.addLast(new IdleStateHandler(60, 30, 90, TimeUnit.SECONDS));

        // 心跳处理器
        pipeline.addLast(heartbeatHandler);

        // WebSocket消息处理器
        pipeline.addLast(webSocketHandler);
    }
}