package com.kwp.chat.service.impl;

import com.kwp.chat.common.constant.CommonConstant;
import com.kwp.chat.common.exception.BusinessException;
import com.kwp.chat.common.result.ResultCode;
import com.kwp.chat.dao.ConversationMemberMapper;
import com.kwp.chat.dao.MessageMapper;
import com.kwp.chat.dao.MessageReadMapper;
import com.kwp.chat.model.message.ConversationMember;
import com.kwp.chat.model.message.Message;
import com.kwp.chat.model.message.MessageRead;
import com.kwp.chat.service.ConversationService;
import com.kwp.chat.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final MessageReadMapper messageReadMapper;
    private final ConversationMemberMapper conversationMemberMapper;
    private final ConversationService conversationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message sendMessage(Long conversationId, Long senderId, Integer messageType, String content,
                               String fileUrl, String fileName, Long fileSize, String fileType,
                               String thumbnailUrl, Integer duration, Long replyMessageId,
                               String atUserIds, String clientMessageId) {
        // 检查是否是会话成员
        ConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(conversationId, senderId);
        if (member == null) {
            throw new BusinessException(ResultCode.NOT_GROUP_MEMBER);
        }

        // 检查客户端消息ID是否重复
        if (clientMessageId != null) {
            Message existingMessage = messageMapper.selectByClientMessageId(clientMessageId);
            if (existingMessage != null) {
                return existingMessage;
            }
        }

        // 创建消息
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setMessageType(messageType);
        message.setContent(content);
        message.setFileUrl(fileUrl);
        message.setFileName(fileName);
        message.setFileSize(fileSize);
        message.setFileType(fileType);
        message.setThumbnailUrl(thumbnailUrl);
        message.setDuration(duration);
        message.setReplyMessageId(replyMessageId);
        message.setAtUserIds(atUserIds);
        message.setStatus(0);
        message.setIsRead(0);
        message.setClientMessageId(clientMessageId);

        messageMapper.insert(message);

        // 更新会话最后消息
        String lastMessageContent = getLastMessageContent(messageType, content, fileName);
        conversationService.updateLastMessage(conversationId, message.getId(), lastMessageContent, senderId);

        // 增加其他成员未读消息数
        conversationMemberMapper.incrementUnreadCount(conversationId, senderId);

        log.info("消息发送成功: conversationId={}, senderId={}, messageId={}", conversationId, senderId, message.getId());

        return message;
    }

    @Override
    public List<Message> getMessages(Long conversationId, Long userId, int page, int size) {
        // 检查是否是会话成员
        ConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            throw new BusinessException(ResultCode.NOT_GROUP_MEMBER);
        }

        int offset = (page - 1) * size;
        return messageMapper.selectByConversationId(conversationId, size, offset);
    }

    @Override
    public Message getMessage(Long messageId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(ResultCode.MESSAGE_NOT_FOUND);
        }
        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recallMessage(Long messageId, Long userId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(ResultCode.MESSAGE_NOT_FOUND);
        }

        // 只能撤回自己的消息
        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 检查撤回时间限制（2分钟）
        long timeDiff = System.currentTimeMillis() - message.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        if (timeDiff > CommonConstant.MESSAGE_RECALL_TIME_LIMIT) {
            throw new BusinessException(ResultCode.MESSAGE_RECALL_TIMEOUT);
        }

        // 更新消息状态
        message.setStatus(1);
        messageMapper.updateById(message);

        log.info("消息撤回成功: messageId={}, userId={}", messageId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markMessageAsRead(Long messageId, Long userId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            return;
        }

        // 不能标记自己的消息为已读
        if (message.getSenderId().equals(userId)) {
            return;
        }

        // 检查是否已标记
        MessageRead existingRead = messageReadMapper.selectByMessageIdAndUserId(messageId, userId);
        if (existingRead != null) {
            return;
        }

        // 创建已读记录
        MessageRead messageRead = new MessageRead();
        messageRead.setMessageId(messageId);
        messageRead.setUserId(userId);
        messageRead.setReadTime(LocalDateTime.now());
        messageReadMapper.insert(messageRead);

        // 更新消息的已读状态
        message.setIsRead(1);
        messageMapper.updateById(message);

        log.info("消息已标记为已读: messageId={}, userId={}", messageId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markConversationMessagesAsRead(Long conversationId, Long userId) {
        // 获取会话中所有未读且不是自己发送的消息
        List<Message> unreadMessages = messageMapper.selectUnreadByConversationId(conversationId, userId);

        for (Message message : unreadMessages) {
            // 检查是否已有已读记录
            MessageRead existingRead = messageReadMapper.selectByMessageIdAndUserId(message.getId(), userId);
            if (existingRead == null) {
                // 创建已读记录
                MessageRead messageRead = new MessageRead();
                messageRead.setMessageId(message.getId());
                messageRead.setUserId(userId);
                messageRead.setReadTime(LocalDateTime.now());
                messageReadMapper.insert(messageRead);
            }

            // 更新消息的已读状态
            message.setIsRead(1);
            messageMapper.updateById(message);
        }

        // 更新会话成员的最后读取时间
        conversationMemberMapper.updateLastReadTime(conversationId, userId, LocalDateTime.now());

        log.info("会话消息已全部标记为已读: conversationId={}, userId={}, count={}", conversationId, userId, unreadMessages.size());
    }

    @Override
    public int getUnreadMessageCount(Long conversationId, Long userId) {
        ConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            return 0;
        }
        return member.getUnreadCount();
    }

    @Override
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(ResultCode.MESSAGE_NOT_FOUND);
        }

        // 只能删除自己的消息
        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 设置status=2（已删除）和deleted=1（逻辑删除）
        message.setStatus(2);
        message.setDeleted(1);
        messageMapper.updateById(message);

        log.info("消息已删除: messageId={}, userId={}", messageId, userId);
    }

    /**
     * 获取最后消息内容显示
     */
    private String getLastMessageContent(Integer messageType, String content, String fileName) {
        if (messageType == CommonConstant.MESSAGE_TYPE_TEXT) {
            return content;
        } else if (messageType == CommonConstant.MESSAGE_TYPE_IMAGE) {
            return "[图片]";
        } else if (messageType == CommonConstant.MESSAGE_TYPE_FILE) {
            return "[文件] " + fileName;
        } else if (messageType == CommonConstant.MESSAGE_TYPE_VIDEO) {
            return "[视频]";
        } else if (messageType == CommonConstant.MESSAGE_TYPE_VOICE) {
            return "[语音]";
        } else if (messageType == CommonConstant.MESSAGE_TYPE_SYSTEM) {
            return content;
        } else {
            return content;
        }
    }
}