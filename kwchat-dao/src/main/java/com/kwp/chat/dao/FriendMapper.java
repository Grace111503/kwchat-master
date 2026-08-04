package com.kwp.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kwp.chat.model.friend.Friend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 好友Mapper接口
 */
@Mapper
public interface FriendMapper extends BaseMapper<Friend> {

    /**
     * 查询好友列表
     */
    @Select("SELECT * FROM sys_friend WHERE user_id = #{userId} AND deleted = 0")
    List<Friend> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询好友关系
     */
    @Select("SELECT * FROM sys_friend WHERE user_id = #{userId} AND friend_id = #{friendId} AND deleted = 0")
    Friend selectByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /**
     * 检查是否是好友
     */
    @Select("SELECT COUNT(*) FROM sys_friend WHERE user_id = #{userId} AND friend_id = #{friendId} AND deleted = 0")
    int countByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /**
     * 检查 userId 是否拉黑了 friendId
     */
    @Select("SELECT COUNT(*) FROM sys_friend WHERE user_id = #{userId} AND friend_id = #{friendId} AND is_black = 1 AND deleted = 0")
    int countBlacklist(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /**
     * 检查单聊会话中是否存在拉黑关系（任一方拉黑另一方）
     */
    @Select("SELECT COUNT(*) FROM sys_friend WHERE " +
            "((user_id = #{userId1} AND friend_id = #{userId2}) OR (user_id = #{userId2} AND friend_id = #{userId1})) " +
            "AND is_black = 1 AND deleted = 0")
    int countAnyBlacklist(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}