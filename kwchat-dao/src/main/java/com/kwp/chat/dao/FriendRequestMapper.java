package com.kwp.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kwp.chat.model.friend.FriendRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 好友申请Mapper接口
 */
@Mapper
public interface FriendRequestMapper extends BaseMapper<FriendRequest> {

    /**
     * 查询收到的好友申请
     */
    @Select("SELECT * FROM sys_friend_request WHERE receiver_id = #{receiverId} AND deleted = 0 ORDER BY create_time DESC")
    List<FriendRequest> selectByReceiverId(@Param("receiverId") Long receiverId);

    /**
     * 查询发出的好友申请
     */
    @Select("SELECT * FROM sys_friend_request WHERE sender_id = #{senderId} AND deleted = 0 ORDER BY create_time DESC")
    List<FriendRequest> selectBySenderId(@Param("senderId") Long senderId);

    /**
     * 查询好友申请
     */
    @Select("SELECT * FROM sys_friend_request WHERE sender_id = #{senderId} AND receiver_id = #{receiverId} AND deleted = 0")
    FriendRequest selectBySenderIdAndReceiverId(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    /**
     * 检查是否已发送好友申请
     */
    @Select("SELECT COUNT(*) FROM sys_friend_request WHERE sender_id = #{senderId} AND receiver_id = #{receiverId} AND status = 0 AND deleted = 0")
    int countPendingBySenderIdAndReceiverId(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
}