package com.kwp.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kwp.chat.model.message.MessageFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 消息收藏Mapper接口
 */
@Mapper
public interface MessageFavoriteMapper extends BaseMapper<MessageFavorite> {

    /**
     * 查询用户是否已收藏消息
     */
    @Select("SELECT * FROM chat_message_favorite WHERE message_id = #{messageId} AND user_id = #{userId} AND deleted = 0")
    MessageFavorite selectByMessageIdAndUserId(@Param("messageId") Long messageId, @Param("userId") Long userId);

    /**
     * 查询用户收藏的消息列表
     */
    @Select("SELECT * FROM chat_message_favorite WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    List<MessageFavorite> selectByUserId(@Param("userId") Long userId);
}
