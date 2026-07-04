package com.kwp.chat.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * WebSocket会话信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WsSession implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 连接时间
     */
    private LocalDateTime connectTime;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeatTime;

    /**
     * 客户端类型（web、pc、mobile）
     */
    private String clientType;

    /**
     * 客户端IP
     */
    private String clientIp;
}