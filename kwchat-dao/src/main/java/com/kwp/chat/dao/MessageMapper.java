package com.kwp.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kwp.chat.model.message.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 消息Mapper接口
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 查询会话消息列表（分页）
     */
    @Select("SELECT * FROM chat_message WHERE conversation_id = #{conversationId} AND deleted = 0 AND status != 2 " +
            "ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<Message> selectByConversationId(@Param("conversationId") Long conversationId, @Param("limit") int limit, @Param("offset") int offset);

    /**
     * 查询会话最新消息
     */
    @Select("SELECT * FROM chat_message WHERE conversation_id = #{conversationId} AND deleted = 0 AND status != 2 " +
            "ORDER BY create_time DESC LIMIT 1")
    Message selectLatestByConversationId(@Param("conversationId") Long conversationId);

    /**
     * 查询消息详情
     */
    @Select("SELECT * FROM chat_message WHERE id = #{id} AND deleted = 0 AND status != 2")
    Message selectById(@Param("id") Long id);

    /**
     * 根据客户端消息ID查询
     */
    @Select("SELECT * FROM chat_message WHERE client_message_id = #{clientMessageId} AND deleted = 0 AND status != 2")
    Message selectByClientMessageId(@Param("clientMessageId") String clientMessageId);

    /**
     * 查询会话中未读的消息（排除自己发送的）
     */
    @Select("SELECT * FROM chat_message WHERE conversation_id = #{conversationId} AND sender_id != #{userId} AND is_read = 0 AND deleted = 0 AND status != 2 " +
            "ORDER BY create_time ASC")
    List<Message> selectUnreadByConversationId(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}