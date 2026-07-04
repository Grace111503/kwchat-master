package com.kwp.chat.websocket.server;

import com.kwp.chat.websocket.handler.WebSocketInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Netty WebSocket服务器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NettyServer {

    private final WebSocketInitializer webSocketInitializer;

    @Value("${netty.websocket.port:9092}")
    private int port;

    @Value("${netty.websocket.boss-threads:1}")
    private int bossThreads;

    @Value("${netty.websocket.worker-threads:4}")
    private int workerThreads;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;

    /**
     * 启动服务器
     */
    @PostConstruct
    public void start() {
        new Thread(() -> {
            try {
                doStart();
            } catch (Exception e) {
                log.error("Netty服务器启动失败", e);
            }
        }).start();
    }

    /**
     * 实际启动逻辑
     */
    private void doStart() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(bossThreads);
        workerGroup = new NioEventLoopGroup(workerThreads);

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(webSocketInitializer);

        channelFuture = bootstrap.bind(port).sync();

        log.info("========================================");
        log.info("   Netty WebSocket服务器启动成功！");
        log.info("   端口: {}", port);
        log.info("   WebSocket地址: ws://localhost:{}/ws", port);
        log.info("========================================");

        channelFuture.channel().closeFuture().sync();
    }

    /**
     * 停止服务器
     */
    @PreDestroy
    public void stop() {
        log.info("正在关闭Netty服务器...");

        if (channelFuture != null) {
            channelFuture.channel().close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }

        log.info("Netty服务器已关闭");
    }
}