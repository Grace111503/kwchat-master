package com.kwp.chat.websocket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kwp.chat.common.model.WsMessage;
import com.kwp.chat.websocket.manager.ChannelManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * WebSocket消息推送服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final ChannelManager channelManager;
    private final ObjectMapper objectMapper;

    /**
     * 发送消息给用户
     */
    public void sendToUser(Long userId, String type, Object data) {
        try {
            WsMessage<Object> message = WsMessage.of(type, data);
            String json = objectMapper.writeValueAsString(message);
            channelManager.sendToUser(userId, json);
            log.debug("发送消息给用户: userId={}, type={}", userId, type);
        } catch (Exception e) {
            log.error("发送消息失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 发送消息给多个用户
     */
    public void sendToUsers(Set<Long> userIds, String type, Object data) {
        for (Long userId : userIds) {
            sendToUser(userId, type, data);
        }
    }

    /**
     * 发送新消息通知
     */
    public void sendNewMessageNotify(Long receiverId, Object messageData) {
        sendToUser(receiverId, "new_message", messageData);
    }

    /**
     * 发送已读回执
     */
    public void sendReadReceipt(Long userId, Object receiptData) {
        sendToUser(userId, "read_receipt", receiptData);
    }

    /**
     * 发送好友申请通知
     */
    public void sendFriendRequestNotify(Long receiverId, Object requestData) {
        sendToUser(receiverId, "friend_request", requestData);
    }

    /**
     * 发送好友申请处理通知
     */
    public void sendFriendRequestHandleNotify(Long senderId, Object handleData) {
        sendToUser(senderId, "friend_request_handle", handleData);
    }

    /**
     * 发送系统通知
     */
    public void sendSystemNotify(Long userId, Object notifyData) {
        sendToUser(userId, "system", notifyData);
    }

    /**
     * 广播系统通知
     */
    public void broadcastSystemNotify(Object notifyData) {
        try {
            WsMessage<Object> message = WsMessage.of("system", notifyData);
            String json = objectMapper.writeValueAsString(message);
            channelManager.broadcast(json);
        } catch (Exception e) {
            log.error("广播系统通知失败: {}", e.getMessage());
        }
    }

    /**
     * 用户是否在线
     */
    public boolean isOnline(Long userId) {
        return channelManager.isOnline(userId);
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineUserCount() {
        return channelManager.getOnlineUserCount();
    }

    /**
     * 获取总连接数
     */
    public int getTotalConnections() {
        return channelManager.getTotalConnections();
    }
}