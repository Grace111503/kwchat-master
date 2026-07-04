package com.kwp.chat.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kwp.chat.common.model.WsMessage;
import com.kwp.chat.common.model.WsSession;
import com.kwp.chat.common.utils.JwtUtils;
import com.kwp.chat.websocket.manager.ChannelManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * WebSocket处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final ChannelManager channelManager;
    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        log.info("WebSocket连接建立: channelId={}", channel.id());

        // 检查URL中是否携带token（由WebSocketAuthHandler提取）
        String urlToken = channel.attr(WebSocketAuthHandler.TOKEN_ATTR).get();
        if (urlToken != null && !urlToken.isEmpty()) {
            // URL中携带了token，自动进行认证
            autoAuth(channel, urlToken, channel.attr(WebSocketAuthHandler.CLIENT_TYPE_ATTR).get());
        }

        super.channelActive(ctx);
    }

    /**
     * 从URL token自动认证
     */
    private void autoAuth(Channel channel, String token, String clientType) {
        try {
            if (!jwtUtils.validateToken(token)) {
                log.warn("URL token无效: channelId={}", channel.id());
                sendError(channel, "Token无效");
                channel.close();
                return;
            }

            Long userId = jwtUtils.getUserId(token);
            String username = jwtUtils.getUsername(token);

            WsSession session = WsSession.builder()
                    .userId(userId)
                    .username(username)
                    .connectTime(LocalDateTime.now())
                    .lastHeartbeatTime(LocalDateTime.now())
                    .clientType(clientType != null ? clientType : "web")
                    .clientIp(channel.remoteAddress().toString())
                    .build();

            channelManager.addChannel(userId, channel, session);

            WsMessage<Object> response = WsMessage.of("auth_success", null, userId);
            channelManager.sendToChannel(channel, objectMapper.writeValueAsString(response));

            log.info("WebSocket URL自动认证成功: userId={}, username={}", userId, username);
        } catch (Exception e) {
            log.error("WebSocket URL自动认证失败: {}", e.getMessage(), e);
            sendError(channel, "认证失败");
            channel.close();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        Channel channel = ctx.channel();

        if (frame instanceof TextWebSocketFrame) {
            String text = ((TextWebSocketFrame) frame).text();
            handleMessage(channel, text);
        } else {
            log.warn("不支持的WebSocket帧类型: {}", frame.getClass().getSimpleName());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channelManager.removeChannel(channel);
        log.info("WebSocket连接断开: channelId={}", channel.id());
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("WebSocket异常: channelId={}, error={}", ctx.channel().id(), cause.getMessage(), cause);
        ctx.close();
        super.exceptionCaught(ctx, cause);
    }

    /**
     * 处理消息
     */
    private void handleMessage(Channel channel, String text) {
        try {
            WsMessage<?> message = objectMapper.readValue(text, WsMessage.class);
            String type = message.getType();

            switch (type) {
                case "auth":
                    handleAuth(channel, text);
                    break;
                case "heartbeat":
                    handleHeartbeat(channel);
                    break;
                case "message":
                    handleChatMessage(channel, message);
                    break;
                case "read":
                    handleReadReceipt(channel, message);
                    break;
                case "typing":
                    handleTyping(channel, message);
                    break;
                default:
                    log.warn("未知的消息类型: {}", type);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败: {}", e.getMessage(), e);
            sendError(channel, "消息格式错误");
        }
    }

    /**
     * 处理认证
     */
    private void handleAuth(Channel channel, String text) {
        try {
            // 如果已经通过URL自动认证，跳过重复认证
            if (channelManager.getUserId(channel) != null) {
                log.debug("用户已认证，跳过重复认证: channelId={}", channel.id());
                return;
            }

            WsMessage<AuthData> message = objectMapper.readValue(text,
                    objectMapper.getTypeFactory().constructParametricType(WsMessage.class, AuthData.class));

            AuthData authData = message.getData();
            if (authData == null || authData.getToken() == null) {
                sendError(channel, "认证信息缺失");
                channel.close();
                return;
            }

            // 验证Token
            String token = authData.getToken();
            if (!jwtUtils.validateToken(token)) {
                sendError(channel, "Token无效");
                channel.close();
                return;
            }

            // 获取用户信息
            Long userId = jwtUtils.getUserId(token);
            String username = jwtUtils.getUsername(token);

            // 创建会话
            WsSession session = WsSession.builder()
                    .userId(userId)
                    .username(username)
                    .connectTime(LocalDateTime.now())
                    .lastHeartbeatTime(LocalDateTime.now())
                    .clientType(authData.getClientType())
                    .clientIp(channel.remoteAddress().toString())
                    .build();

            // 添加到通道管理器
            channelManager.addChannel(userId, channel, session);

            // 发送认证成功消息
            WsMessage<Object> response = WsMessage.of("auth_success", null, userId);
            channelManager.sendToChannel(channel, objectMapper.writeValueAsString(response));

            log.info("WebSocket认证成功: userId={}, username={}", userId, username);

        } catch (Exception e) {
            log.error("WebSocket认证失败: {}", e.getMessage(), e);
            sendError(channel, "认证失败");
            channel.close();
        }
    }

    /**
     * 处理心跳
     */
    private void handleHeartbeat(Channel channel) {
        Long userId = channelManager.getUserId(channel);
        if (userId == null) {
            return;
        }

        // 更新心跳时间
        WsSession session = channelManager.getSession(channel);
        if (session != null) {
            session.setLastHeartbeatTime(LocalDateTime.now());
            channelManager.updateSession(channel, session);
        }

        // 发送心跳响应
        try {
            WsMessage<Void> heartbeat = WsMessage.heartbeat();
            channelManager.sendToChannel(channel, objectMapper.writeValueAsString(heartbeat));
        } catch (Exception e) {
            log.error("发送心跳响应失败: {}", e.getMessage());
        }
    }

    /**
     * 处理聊天消息
     */
    private void handleChatMessage(Channel channel, WsMessage<?> message) {
        Long senderId = channelManager.getUserId(channel);
        if (senderId == null) {
            sendError(channel, "未认证");
            return;
        }

        // 转发消息给目标用户
        Long receiverId = message.getReceiverId();
        Long conversationId = message.getConversationId();

        if (receiverId != null) {
            // 单聊消息
            channelManager.sendToUser(receiverId, message.toString());
        } else if (conversationId != null) {
            // 群聊消息 - 需要从数据库获取群成员
            // 这里简化处理，实际应该查询群成员
            channelManager.sendToUser(senderId, message.toString());
        }
    }

    /**
     * 处理已读回执
     */
    private void handleReadReceipt(Channel channel, WsMessage<?> message) {
        Long userId = channelManager.getUserId(channel);
        if (userId == null) {
            return;
        }

        // 转发已读回执
        Long receiverId = message.getReceiverId();
        if (receiverId != null) {
            channelManager.sendToUser(receiverId, message.toString());
        }
    }

    /**
     * 处理正在输入
     */
    private void handleTyping(Channel channel, WsMessage<?> message) {
        Long userId = channelManager.getUserId(channel);
        if (userId == null) {
            return;
        }

        // 转发输入状态
        Long receiverId = message.getReceiverId();
        if (receiverId != null) {
            channelManager.sendToUser(receiverId, message.toString());
        }
    }

    /**
     * 发送错误消息
     */
    private void sendError(Channel channel, String errorMessage) {
        try {
            WsMessage<Object> error = WsMessage.of("error", errorMessage);
            channelManager.sendToChannel(channel, objectMapper.writeValueAsString(error));
        } catch (Exception e) {
            log.error("发送错误消息失败: {}", e.getMessage());
        }
    }

    /**
     * 认证数据
     */
    @lombok.Data
    private static class AuthData {
        private String token;
        private String clientType;
    }
}