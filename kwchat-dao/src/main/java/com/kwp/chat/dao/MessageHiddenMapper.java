package com.kwp.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kwp.chat.model.message.MessageHidden;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户隐藏消息Mapper接口
 */
@Mapper
public interface MessageHiddenMapper extends BaseMapper<MessageHidden> {

    /**
     * 查询用户是否隐藏了某条消息
     */
    @Select("SELECT * FROM chat_message_hidden WHERE message_id = #{messageId} AND user_id = #{userId} AND deleted = 0")
    MessageHidden selectByMessageIdAndUserId(@Param("messageId") Long messageId, @Param("userId") Long userId);

    /**
     * 查询用户隐藏的所有消息ID
     */
    @Select("SELECT message_id FROM chat_message_hidden WHERE user_id = #{userId} AND deleted = 0")
    List<Long> selectHiddenMessageIdsByUserId(@Param("userId") Long userId);
}
