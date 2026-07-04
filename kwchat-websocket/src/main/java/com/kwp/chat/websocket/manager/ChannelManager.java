package com.kwp.chat.websocket.manager;

import com.kwp.chat.common.model.WsSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 通道管理器 - 管理WebSocket连接
 */
@Slf4j
@Component
public class ChannelManager {

    /**
     * 用户ID -> Channel集合（一个用户可能有多个客户端连接）
     */
    private final Map<Long, Set<Channel>> userChannels = new ConcurrentHashMap<>();

    /**
     * Channel -> 用户ID
     */
    private final Map<ChannelId, Long> channelUsers = new ConcurrentHashMap<>();

    /**
     * Channel -> 会话信息
     */
    private final Map<ChannelId, WsSession> channelSessions = new ConcurrentHashMap<>();

    /**
     * 添加连接
     */
    public void addChannel(Long userId, Channel channel, WsSession session) {
        // 添加到用户通道集合
        userChannels.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(channel);

        // 添加通道到用户映射
        channelUsers.put(channel.id(), userId);

        // 添加会话信息
        channelSessions.put(channel.id(), session);

        log.info("用户连接: userId={}, channelId={}, 总连接数={}", userId, channel.id(), getTotalConnections());
    }

    /**
     * 移除连接
     */
    public void removeChannel(Channel channel) {
        Long userId = channelUsers.remove(channel.id());
        if (userId != null) {
            Set<Channel> channels = userChannels.get(userId);
            if (channels != null) {
                channels.remove(channel);
                if (channels.isEmpty()) {
                    userChannels.remove(userId);
                }
            }
            channelSessions.remove(channel.id());

            log.info("用户断开连接: userId={}, channelId={}, 剩余连接数={}", userId, channel.id(), getTotalConnections());
        }
    }

    /**
     * 获取用户的所有通道
     */
    public Set<Channel> getUserChannels(Long userId) {
        return userChannels.getOrDefault(userId, new CopyOnWriteArraySet<>());
    }

    /**
     * 获取通道对应的用户ID
     */
    public Long getUserId(Channel channel) {
        return channelUsers.get(channel.id());
    }

    /**
     * 获取通道会话信息
     */
    public WsSession getSession(Channel channel) {
        return channelSessions.get(channel.id());
    }

    /**
     * 更新会话信息
     */
    public void updateSession(Channel channel, WsSession session) {
        channelSessions.put(channel.id(), session);
    }

    /**
     * 用户是否在线
     */
    public boolean isOnline(Long userId) {
        Set<Channel> channels = userChannels.get(userId);
        return channels != null && !channels.isEmpty();
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineUserCount() {
        return userChannels.size();
    }

    /**
     * 获取总连接数
     */
    public int getTotalConnections() {
        return channelUsers.size();
    }

    /**
     * 向用户发送消息
     */
    public void sendToUser(Long userId, String message) {
        Set<Channel> channels = getUserChannels(userId);
        for (Channel channel : channels) {
            if (channel.isActive()) {
                channel.writeAndFlush(new TextWebSocketFrame(message));
            }
        }
    }

    /**
     * 向多个用户发送消息
     */
    public void sendToUsers(Set<Long> userIds, String message) {
        for (Long userId : userIds) {
            sendToUser(userId, message);
        }
    }

    /**
     * 向通道发送消息
     */
    public void sendToChannel(Channel channel, String message) {
        if (channel.isActive()) {
            channel.writeAndFlush(new TextWebSocketFrame(message));
        }
    }

    /**
     * 广播消息给所有在线用户
     */
    public void broadcast(String message) {
        for (Set<Channel> channels : userChannels.values()) {
            for (Channel channel : channels) {
                if (channel.isActive()) {
                    channel.writeAndFlush(new TextWebSocketFrame(message));
                }
            }
        }
    }

    /**
     * 关闭用户的所有连接
     */
    public void closeUserConnections(Long userId) {
        Set<Channel> channels = getUserChannels(userId);
        for (Channel channel : channels) {
            channel.close();
        }
    }

    /**
     * 关闭所有连接
     */
    public void closeAll() {
        for (Set<Channel> channels : userChannels.values()) {
            for (Channel channel : channels) {
                channel.close();
            }
        }
        userChannels.clear();
        channelUsers.clear();
        channelSessions.clear();
    }
}