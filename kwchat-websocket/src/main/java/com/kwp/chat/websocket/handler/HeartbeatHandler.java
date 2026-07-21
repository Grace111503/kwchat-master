package com.kwp.chat.websocket.handler;

import com.kwp.chat.common.model.WsSession;
import com.kwp.chat.websocket.manager.ChannelManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 心跳检测处理器
 */
@Slf4j
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {

    private final ChannelManager channelManager;

    public HeartbeatHandler(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    /**
     * 心跳超时时间（秒）
     */
    private static final int HEARTBEAT_TIMEOUT = 60;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            Channel channel = ctx.channel();

            if (event.state() == IdleState.READER_IDLE) {
                // 读空闲，检查心跳超时
                handleReaderIdle(channel);
            } else if (event.state() == IdleState.WRITER_IDLE) {
                // 写空闲，可以发送心跳
                handleWriterIdle(channel);
            } else if (event.state() == IdleState.ALL_IDLE) {
                // 全部空闲
                handleAllIdle(channel);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 处理读空闲
     */
    private void handleReaderIdle(Channel channel) {
        Long userId = channelManager.getUserId(channel);
        if (userId == null) {
            return;
        }

        WsSession session = channelManager.getSession(channel);
        if (session == null) {
            return;
        }

        // 检查心跳超时
        LocalDateTime lastHeartbeat = session.getLastHeartbeatTime();
        if (lastHeartbeat != null) {
            long seconds = ChronoUnit.SECONDS.between(lastHeartbeat, LocalDateTime.now());
            if (seconds > HEARTBEAT_TIMEOUT) {
                log.warn("心跳超时，关闭连接: userId={}, channelId={}, 超时{}秒", userId, channel.id(), seconds);
                channel.close();
            }
        }
    }

    /**
     * 处理写空闲
     */
    private void handleWriterIdle(Channel channel) {
        // 可以在这里发送心跳检测
        log.debug("写空闲: channelId={}", channel.id());
    }

    /**
     * 处理全部空闲
     */
    private void handleAllIdle(Channel channel) {
        log.debug("全部空闲: channelId={}", channel.id());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("连接断开: channelId={}", ctx.channel().id());
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("心跳处理异常: channelId={}, error={}", ctx.channel().id(), cause.getMessage());
        ctx.close();
        super.exceptionCaught(ctx, cause);
    }
}