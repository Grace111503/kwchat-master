package com.kwp.chat.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * WebSocket消息体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WsMessage<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID（单聊时）
     */
    private Long receiverId;

    /**
     * 会话ID（群聊时）
     */
    private Long conversationId;

    /**
     * 创建心跳消息
     */
    public static WsMessage<Void> heartbeat() {
        return WsMessage.<Void>builder()
                .type("heartbeat")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建文本消息
     */
    public static <T> WsMessage<T> of(String type, T data) {
        return WsMessage.<T>builder()
                .type(type)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建带发送者的消息
     */
    public static <T> WsMessage<T> of(String type, T data, Long senderId) {
        return WsMessage.<T>builder()
                .type(type)
                .data(data)
                .senderId(senderId)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}