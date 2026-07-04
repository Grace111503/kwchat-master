package com.kwp.chat.service;

import com.kwp.chat.model.message.Message;

import java.util.List;

/**
 * 消息服务接口
 */
public interface MessageService {

    /**
     * 发送消息
     */
    Message sendMessage(Long conversationId, Long senderId, Integer messageType, String content,
                        String fileUrl, String fileName, Long fileSize, String fileType,
                        String thumbnailUrl, Integer duration, Long replyMessageId,
                        String atUserIds, String clientMessageId);

    /**
     * 获取会话消息列表
     */
    List<Message> getMessages(Long conversationId, Long userId, int page, int size);

    /**
     * 获取消息详情
     */
    Message getMessage(Long messageId);

    /**
     * 撤回消息
     */
    void recallMessage(Long messageId, Long userId);

    /**
     * 标记消息已读
     */
    void markMessageAsRead(Long messageId, Long userId);

    /**
     * 标记会话消息已读
     */
    void markConversationMessagesAsRead(Long conversationId, Long userId);

    /**
     * 获取未读消息数
     */
    int getUnreadMessageCount(Long conversationId, Long userId);
}