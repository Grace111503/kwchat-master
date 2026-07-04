package com.kwp.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kwp.chat.model.message.MessageRead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 消息已读状态Mapper接口
 */
@Mapper
public interface MessageReadMapper extends BaseMapper<MessageRead> {

    /**
     * 查询消息已读状态
     */
    @Select("SELECT * FROM chat_message_read WHERE message_id = #{messageId} AND user_id = #{userId} AND deleted = 0")
    MessageRead selectByMessageIdAndUserId(@Param("messageId") Long messageId, @Param("userId") Long userId);

    /**
     * 检查消息是否已读
     */
    @Select("SELECT COUNT(*) FROM chat_message_read WHERE message_id = #{messageId} AND user_id = #{userId} AND deleted = 0")
    int countByMessageIdAndUserId(@Param("messageId") Long messageId, @Param("userId") Long userId);
}