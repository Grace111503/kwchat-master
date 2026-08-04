package com.kwp.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kwp.chat.model.message.ConversationMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话成员Mapper接口
 */
@Mapper
public interface ConversationMemberMapper extends BaseMapper<ConversationMember> {

    /**
     * 查询会话成员列表
     */
    @Select("SELECT * FROM chat_conversation_member WHERE conversation_id = #{conversationId} AND deleted = 0")
    List<ConversationMember> selectByConversationId(@Param("conversationId") Long conversationId);

    /**
     * 查询会话成员
     */
    @Select("SELECT * FROM chat_conversation_member WHERE conversation_id = #{conversationId} AND user_id = #{userId} AND deleted = 0")
    ConversationMember selectByConversationIdAndUserId(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    /**
     * 更新最后读取时间
     */
    @Update("UPDATE chat_conversation_member SET last_read_time = #{lastReadTime}, unread_count = 0 " +
            "WHERE conversation_id = #{conversationId} AND user_id = #{userId} AND deleted = 0")
    int updateLastReadTime(@Param("conversationId") Long conversationId, @Param("userId") Long userId, @Param("lastReadTime") LocalDateTime lastReadTime);

    /**
     * 增加未读消息数
     */
    @Update("UPDATE chat_conversation_member SET unread_count = unread_count + 1 " +
            "WHERE conversation_id = #{conversationId} AND user_id != #{senderId} AND deleted = 0")
    int incrementUnreadCount(@Param("conversationId") Long conversationId, @Param("senderId") Long senderId);

    /**
     * 查询会话中所有成员的用户ID
     */
    @Select("SELECT user_id FROM chat_conversation_member WHERE conversation_id = #{conversationId} AND deleted = 0")
    List<Long> selectUserIdsByConversationId(@Param("conversationId") Long conversationId);

    /**
     * 获取单聊会话中的对方成员
     */
    @Select("SELECT * FROM chat_conversation_member WHERE conversation_id = #{conversationId} AND user_id != #{userId} AND deleted = 0 LIMIT 1")
    ConversationMember selectOtherMember(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}