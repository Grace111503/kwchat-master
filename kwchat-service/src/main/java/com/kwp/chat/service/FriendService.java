package com.kwp.chat.service;

import com.kwp.chat.model.dto.UserInfoResponse;
import com.kwp.chat.model.friend.FriendRequest;
import com.kwp.chat.model.user.User;

import java.util.List;

/**
 * 好友服务接口
 */
public interface FriendService {

    /**
     * 发送好友申请
     */
    void sendFriendRequest(Long senderId, Long receiverId, String message);

    /**
     * 处理好友申请
     */
    void handleFriendRequest(Long requestId, Long userId, Integer status);

    /**
     * 获取好友列表
     */
    List<UserInfoResponse> getFriendList(Long userId);

    /**
     * 获取收到的好友申请列表
     */
    List<FriendRequest> getReceivedFriendRequests(Long userId);

    /**
     * 获取发出的好友申请列表
     */
    List<FriendRequest> getSentFriendRequests(Long userId);

    /**
     * 删除好友
     */
    void deleteFriend(Long userId, Long friendId);

    /**
     * 更新好友备注
     */
    void updateFriendRemark(Long userId, Long friendId, String remark);

    /**
     * 检查是否是好友
     */
    boolean isFriend(Long userId, Long friendId);

    /**
     * 拉黑好友
     */
    void blackFriend(Long userId, Long friendId);

    /**
     * 取消拉黑
     */
    void unblackFriend(Long userId, Long friendId);

    /**
     * 获取黑名单列表
     */
    List<User> getBlacklist(Long userId);

    /**
     * 更新好友分组
     */
    void updateFriendGroup(Long userId, Long friendId, String groupName);

    /**
     * 获取好友分组列表
     */
    List<String> getFriendGroups(Long userId);
}