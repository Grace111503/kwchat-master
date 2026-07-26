package com.kwp.chat.service;

import com.kwp.chat.model.message.Conversation;
import com.kwp.chat.model.message.ConversationMember;

import java.util.List;

/**
 * 会话服务接口
 */
public interface ConversationService {

    /**
     * 获取用户的会话列表
     */
    List<Conversation> getConversationList(Long userId);

    /**
     * 获取或创建单聊会话
     */
    Conversation getOrCreatePrivateConversation(Long userId1, Long userId2);

    /**
     * 创建群聊会话
     */
    Conversation createGroupConversation(Long creatorId, String name, List<Long> memberIds);

    /**
     * 获取会话详情
     */
    Conversation getConversation(Long conversationId);

    /**
     * 获取会话成员列表
     */
    List<ConversationMember> getConversationMembers(Long conversationId);

    /**
     * 添加会话成员
     */
    void addConversationMember(Long conversationId, Long userId);

    /**
     * 移除会话成员
     */
    void removeConversationMember(Long conversationId, Long userId);

    /**
     * 更新会话最后消息
     */
    void updateLastMessage(Long conversationId, Long messageId, String content, Long senderId);

    /**
     * 清除未读消息数
     */
    void clearUnreadCount(Long conversationId, Long userId);

    /**
     * 设置免打扰
     */
    void setDoNotDisturb(Long conversationId, Long userId, Integer doNotDisturb);

    /**
     * 设置置顶
     */
    void setTop(Long conversationId, Long userId, Integer isTop);

    /**
     * 更新群公告
     */
    void updateAnnouncement(Long conversationId, String announcement);

    /**
     * 更新群名称
     */
    void updateGroupName(Long conversationId, String name);

    /**
     * 更新群头像
     */
    void updateGroupAvatar(Long conversationId, String avatar);

    /**
     * 解散群聊
     */
    void dissolveGroup(Long conversationId, Long userId);

    /**
     * 更新成员角色
     */
    void updateMemberRole(Long conversationId, Long userId, Integer role);

    /**
     * 退出会话
     */
    void exitConversation(Long conversationId, Long userId);
}