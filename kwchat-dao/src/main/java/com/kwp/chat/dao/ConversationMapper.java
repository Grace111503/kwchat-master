package com.kwp.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kwp.chat.model.message.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 会话Mapper接口
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {

    /**
     * 查询用户的会话列表
     */
    @Select("SELECT c.id, c.conversation_type, c.name, c.avatar, c.last_message_id, " + "c.member_count, c.creator_id, c.announcement, " + "cm.is_top, cm.do_not_disturb, " + "c.create_time, c.update_time, c.create_by, c.update_by, c.deleted " +
            "FROM chat_conversation c " +
            "INNER JOIN chat_conversation_member cm ON c.id = cm.conversation_id " +
            "WHERE cm.user_id = #{userId} AND c.deleted = 0 AND cm.deleted = 0 " +
            "ORDER BY cm.is_top DESC, c.last_message_time DESC")
    List<Conversation> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询单聊会话
     */
    @Select("SELECT c.* FROM chat_conversation c " +
            "INNER JOIN chat_conversation_member cm1 ON c.id = cm1.conversation_id AND cm1.user_id = #{userId1} " +
            "INNER JOIN chat_conversation_member cm2 ON c.id = cm2.conversation_id AND cm2.user_id = #{userId2} " +
            "WHERE c.conversation_type = 1 AND c.deleted = 0")
    Conversation selectPrivateConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}