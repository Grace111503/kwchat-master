package com.kwp.chat.service.impl;

import com.kwp.chat.common.constant.CommonConstant;
import com.kwp.chat.common.exception.BusinessException;
import com.kwp.chat.common.result.ResultCode;
import com.kwp.chat.dao.FriendMapper;
import com.kwp.chat.dao.FriendRequestMapper;
import com.kwp.chat.dao.UserMapper;
import com.kwp.chat.model.dto.UserInfoResponse;
import com.kwp.chat.model.friend.Friend;
import com.kwp.chat.model.friend.FriendRequest;
import com.kwp.chat.model.message.Conversation;
import com.kwp.chat.model.message.Message;
import com.kwp.chat.model.user.User;
import com.kwp.chat.service.ConversationService;
import com.kwp.chat.service.FriendService;
import com.kwp.chat.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 好友服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final FriendMapper friendMapper;
    private final FriendRequestMapper friendRequestMapper;
    private final UserMapper userMapper;
    private final ConversationService conversationService;
    private final MessageService messageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendFriendRequest(Long senderId, Long receiverId, String message) {
        // 不能添加自己为好友
        if (senderId.equals(receiverId)) {
            throw new BusinessException(ResultCode.CANNOT_ADD_SELF);
        }

        // 检查接收者是否存在
        User receiver = userMapper.selectById(receiverId);
        if (receiver == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 检查是否已经是好友
        if (friendMapper.countByUserIdAndFriendId(senderId, receiverId) > 0) {
            throw new BusinessException(ResultCode.FRIEND_ALREADY_EXISTS);
        }

        // 检查是否已发送过申请（任何状态）
        FriendRequest existingRequest = friendRequestMapper.selectBySenderIdAndReceiverId(senderId, receiverId);
        if (existingRequest != null) {
            throw new BusinessException(ResultCode.FRIEND_REQUEST_ALREADY_SENT);
        }

        // 创建好友申请
        FriendRequest request = new FriendRequest();
        request.setSenderId(senderId);
        request.setReceiverId(receiverId);
        request.setMessage(message);
        request.setStatus(CommonConstant.FRIEND_REQUEST_PENDING);

        friendRequestMapper.insert(request);

        log.info("发送好友申请: senderId={}, receiverId={}", senderId, receiverId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleFriendRequest(Long requestId, Long userId, Integer status) {
        FriendRequest request = friendRequestMapper.selectById(requestId);
        if (request == null) {
            throw new BusinessException(ResultCode.FRIEND_REQUEST_NOT_FOUND);
        }

        // 验证是否是接收者
        if (!request.getReceiverId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 检查状态
        if (!CommonConstant.FRIEND_REQUEST_PENDING.equals(request.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该申请已处理");
        }

        // 更新申请状态
        request.setStatus(status);
        request.setHandleTime(LocalDateTime.now());
        friendRequestMapper.updateById(request);

        // 如果同意，添加好友关系
        if (CommonConstant.FRIEND_REQUEST_AGREED.equals(status)) {
            addFriend(request.getSenderId(), request.getReceiverId());
            addFriend(request.getReceiverId(), request.getSenderId());

            // 创建会话并发送欢迎消息
            sendWelcomeMessage(request.getSenderId(), request.getReceiverId());

            log.info("好友申请已同意: requestId={}", requestId);
        } else {
            log.info("好友申请已拒绝: requestId={}", requestId);
        }
    }

    @Override
    public List<UserInfoResponse> getFriendList(Long userId) {
        List<Friend> friends = friendMapper.selectByUserId(userId);
        return friends.stream()
                .map(friend -> {
                    User user = userMapper.selectById(friend.getFriendId());
                    if (user == null) {
                        return null;
                    }
                    UserInfoResponse response = new UserInfoResponse();
                    BeanUtils.copyProperties(user, response);
                    return response;
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendRequest> getReceivedFriendRequests(Long userId) {
        return friendRequestMapper.selectByReceiverId(userId);
    }

    @Override
    public List<FriendRequest> getSentFriendRequests(Long userId) {
        return friendRequestMapper.selectBySenderId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFriend(Long userId, Long friendId) {
        // 删除双向好友关系
        Friend friend1 = friendMapper.selectByUserIdAndFriendId(userId, friendId);
        if (friend1 != null) {
            friendMapper.deleteById(friend1.getId());
        }

        Friend friend2 = friendMapper.selectByUserIdAndFriendId(friendId, userId);
        if (friend2 != null) {
            friendMapper.deleteById(friend2.getId());
        }

        log.info("删除好友: userId={}, friendId={}", userId, friendId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFriendRemark(Long userId, Long friendId, String remark) {
        Friend friend = friendMapper.selectByUserIdAndFriendId(userId, friendId);
        if (friend == null) {
            throw new BusinessException(ResultCode.FRIEND_NOT_FOUND);
        }

        friend.setRemark(remark);
        friendMapper.updateById(friend);
    }

    @Override
    public boolean isFriend(Long userId, Long friendId) {
        return friendMapper.countByUserIdAndFriendId(userId, friendId) > 0;
    }

    /**
     * 添加好友关系
     */
    private void addFriend(Long userId, Long friendId) {
        // 检查是否已存在好友关系
        Friend existingFriend = friendMapper.selectByUserIdAndFriendId(userId, friendId);
        if (existingFriend != null) {
            return; // 已存在，跳过
        }

        try {
            Friend friend = new Friend();
            friend.setUserId(userId);
            friend.setFriendId(friendId);
            friend.setIsStar(0);
            friend.setIsBlack(0);
            friendMapper.insert(friend);
        } catch (Exception e) {
            // 忽略重复键异常（并发情况下可能重复插入）
            log.warn("添加好友时忽略重复键异常: userId={}, friendId={}", userId, friendId);
        }
    }

    /**
     * 发送欢迎消息（同意好友请求后自动发送）
     * @param senderId 发送好友申请的人（接收欢迎消息）
     * @param acceptorId 同意好友申请的人（发送欢迎消息）
     */
    private void sendWelcomeMessage(Long senderId, Long acceptorId) {
        try {
            // 获取或创建单聊会话
            Conversation conversation = conversationService.getOrCreatePrivateConversation(acceptorId, senderId);

            // 发送欢迎消息（从同意者发给申请者）
            String welcomeContent = "我通过了你的好友验证，现在可以开始聊天了";
            messageService.sendMessage(
                    conversation.getId(),
                    acceptorId,
                    CommonConstant.MESSAGE_TYPE_TEXT,
                    welcomeContent,
                    null, null, null, null, null, null, null, null, null
            );

            log.info("欢迎消息已发送: from={}, to={}, conversationId={}", acceptorId, senderId, conversation.getId());
        } catch (Exception e) {
            log.error("发送欢迎消息失败: senderId={}, acceptorId={}, error={}", senderId, acceptorId, e.getMessage(), e);
        }
    }

    @Override
    public void blackFriend(Long userId, Long friendId) {
        Friend friend = friendMapper.selectByUserIdAndFriendId(userId, friendId);
        if (friend == null) {
            throw new BusinessException(ResultCode.FRIEND_NOT_FOUND);
        }
        friend.setIsBlack(1);
        friendMapper.updateById(friend);
        log.info("好友已拉黑: userId={}, friendId={}", userId, friendId);
    }

    @Override
    public void unblackFriend(Long userId, Long friendId) {
        Friend friend = friendMapper.selectByUserIdAndFriendId(userId, friendId);
        if (friend == null) {
            throw new BusinessException(ResultCode.FRIEND_NOT_FOUND);
        }
        friend.setIsBlack(0);
        friendMapper.updateById(friend);
        log.info("已取消拉黑: userId={}, friendId={}", userId, friendId);
    }

    @Override
    public List<User> getBlacklist(Long userId) {
        List<Friend> friends = friendMapper.selectByUserId(userId);
        List<Long> blackFriendIds = friends.stream()
                .filter(f -> Integer.valueOf(1).equals(f.getIsBlack()))
                .map(Friend::getFriendId)
                .toList();

        if (blackFriendIds.isEmpty()) {
            return List.of();
        }

        return blackFriendIds.stream()
                .map(userMapper::selectById)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void updateFriendGroup(Long userId, Long friendId, String groupName) {
        Friend friend = friendMapper.selectByUserIdAndFriendId(userId, friendId);
        if (friend == null) {
            throw new BusinessException(ResultCode.FRIEND_NOT_FOUND);
        }
        friend.setGroupName(groupName);
        friendMapper.updateById(friend);
    }

    @Override
    public List<String> getFriendGroups(Long userId) {
        List<Friend> friends = friendMapper.selectByUserId(userId);
        return friends.stream()
                .map(Friend::getGroupName)
                .filter(Objects::nonNull)
                .filter(g -> !g.isEmpty())
                .distinct()
                .sorted()
                .toList();
    }
}