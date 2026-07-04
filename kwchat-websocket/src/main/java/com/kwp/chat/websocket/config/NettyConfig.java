package com.kwp.chat.websocket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Netty配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "netty.websocket")
public class NettyConfig {

    /**
     * WebSocket端口
     */
    private int port = 9092;

    /**
     * Boss线程数
     */
    private int bossThreads = 1;

    /**
     * Worker线程数
     */
    private int workerThreads = 4;

    /**
     * 心跳超时时间（秒）
     */
    private int heartbeatTimeout = 60;
}