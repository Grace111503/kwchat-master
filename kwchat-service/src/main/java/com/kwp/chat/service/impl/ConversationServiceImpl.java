package com.kwp.chat.service.impl;

import com.kwp.chat.common.constant.CommonConstant;
import com.kwp.chat.common.exception.BusinessException;
import com.kwp.chat.common.result.ResultCode;
import com.kwp.chat.dao.ConversationMapper;
import com.kwp.chat.dao.ConversationMemberMapper;
import com.kwp.chat.dao.UserMapper;
import com.kwp.chat.model.message.Conversation;
import com.kwp.chat.model.message.ConversationMember;
import com.kwp.chat.model.user.User;
import com.kwp.chat.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationMapper conversationMapper;
    private final ConversationMemberMapper conversationMemberMapper;
    private final UserMapper userMapper;

    @Override
    public List<Conversation> getConversationList(Long userId) {
        List<Conversation> conversations = conversationMapper.selectByUserId(userId);
        // 为单聊会话填充对方昵称
        for (Conversation conversation : conversations) {
            if (CommonConstant.CONVERSATION_TYPE_PRIVATE.equals(conversation.getConversationType())) {
                fillPrivateConversationName(conversation, userId);
            }
        }
        return conversations;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Conversation getOrCreatePrivateConversation(Long userId1, Long userId2) {
        // 查找是否已存在单聊会话
        Conversation conversation = conversationMapper.selectPrivateConversation(userId1, userId2);
        if (conversation != null) {
            fillPrivateConversationName(conversation, userId1);
            return conversation;
        }

        // 创建新会话
        conversation = new Conversation();
        conversation.setConversationType(CommonConstant.CONVERSATION_TYPE_PRIVATE);
        conversation.setMemberCount(2);
        conversation.setCreatorId(userId1);
        conversation.setDoNotDisturb(0);
        conversation.setIsTop(0);
        conversationMapper.insert(conversation);

        // 添加会话成员
        addMember(conversation.getId(), userId1, CommonConstant.GROUP_ROLE_MEMBER);
        addMember(conversation.getId(), userId2, CommonConstant.GROUP_ROLE_MEMBER);

        // 填充对方昵称
        fillPrivateConversationName(conversation, userId1);

        log.info("创建单聊会话: userId1={}, userId2={}, conversationId={}", userId1, userId2, conversation.getId());

        return conversation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Conversation createGroupConversation(Long creatorId, String name, List<Long> memberIds) {
        // 创建群聊会话
        Conversation conversation = new Conversation();
        conversation.setConversationType(CommonConstant.CONVERSATION_TYPE_GROUP);
        conversation.setName(name);
        conversation.setMemberCount(memberIds.size() + 1);
        conversation.setCreatorId(creatorId);
        conversation.setDoNotDisturb(0);
        conversation.setIsTop(0);
        conversationMapper.insert(conversation);

        // 添加创建者为群主
        addMember(conversation.getId(), creatorId, CommonConstant.GROUP_ROLE_OWNER);

        // 添加其他成员
        for (Long memberId : memberIds) {
            if (!memberId.equals(creatorId)) {
                addMember(conversation.getId(), memberId, CommonConstant.GROUP_ROLE_MEMBER);
            }
        }

        log.info("创建群聊会话: creatorId={}, name={}, conversationId={}", creatorId, name, conversation.getId());

        return conversation;
    }

    @Override
    public Conversation getConversation(Long conversationId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ResultCode.CONVERSATION_NOT_FOUND);
        }
        return conversation;
    }

    @Override
    public List<ConversationMember> getConversationMembers(Long conversationId) {
        return conversationMemberMapper.selectByConversationId(conversationId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addConversationMember(Long conversationId, Long userId) {
        Conversation conversation = getConversation(conversationId);

        // 检查是否已是成员
        ConversationMember existingMember = conversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (existingMember != null) {
            throw new BusinessException(ResultCode.GROUP_MEMBER_ALREADY_EXISTS);
        }

        // 添加成员
        addMember(conversationId, userId, CommonConstant.GROUP_ROLE_MEMBER);

        // 更新成员数量
        conversation.setMemberCount(conversation.getMemberCount() + 1);
        conversationMapper.updateById(conversation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeConversationMember(Long conversationId, Long userId) {
        Conversation conversation = getConversation(conversationId);

        ConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            throw new BusinessException(ResultCode.GROUP_MEMBER_NOT_FOUND);
        }

        // 删除成员
        conversationMemberMapper.deleteById(member.getId());

        // 更新成员数量
        conversation.setMemberCount(conversation.getMemberCount() - 1);
        conversationMapper.updateById(conversation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLastMessage(Long conversationId, Long messageId, String content, Long senderId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            return;
        }

        conversation.setLastMessageId(messageId);
        conversation.setLastMessageContent(content);
        conversation.setLastMessageTime(LocalDateTime.now());
        conversation.setLastMessageSenderId(senderId);
        conversationMapper.updateById(conversation);
    }

    @Override
    public void clearUnreadCount(Long conversationId, Long userId) {
        conversationMemberMapper.updateLastReadTime(conversationId, userId, LocalDateTime.now());
    }

    @Override
    public void setDoNotDisturb(Long conversationId, Long userId, Integer doNotDisturb) {
        ConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            throw new BusinessException(ResultCode.CONVERSATION_NOT_FOUND);
        }

        member.setDoNotDisturb(doNotDisturb);
        conversationMemberMapper.updateById(member);
    }

    @Override
    public void setTop(Long conversationId, Long userId, Integer isTop) {
        ConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            throw new BusinessException(ResultCode.CONVERSATION_NOT_FOUND);
        }

        member.setIsTop(isTop);
        conversationMemberMapper.updateById(member);
    }

    /**
     * 添加会话成员
     */
    private void addMember(Long conversationId, Long userId, Integer role) {
        User user = userMapper.selectById(userId);

        ConversationMember member = new ConversationMember();
        member.setConversationId(conversationId);
        member.setUserId(userId);
        member.setNickname(user != null ? user.getNickname() : null);
        member.setRole(role);
        member.setDoNotDisturb(0);
        member.setIsTop(0);
        member.setUnreadCount(0);
        member.setJoinTime(LocalDateTime.now());

        conversationMemberMapper.insert(member);
    }

    /**
     * 为单聊会话填充对方昵称
     */
    private void fillPrivateConversationName(Conversation conversation, Long currentUserId) {
        List<ConversationMember> members = conversationMemberMapper.selectByConversationId(conversation.getId());
        for (ConversationMember member : members) {
            if (!member.getUserId().equals(currentUserId)) {
                User otherUser = userMapper.selectById(member.getUserId());
                if (otherUser != null) {
                    conversation.setName(otherUser.getNickname());
                    conversation.setAvatar(otherUser.getAvatar());
                }
                break;
            }
        }
    }
}