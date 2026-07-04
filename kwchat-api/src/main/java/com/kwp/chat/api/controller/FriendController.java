package com.kwp.chat.api.controller;

import com.kwp.chat.common.result.Result;
import com.kwp.chat.model.dto.UserInfoResponse;
import com.kwp.chat.model.friend.FriendRequest;
import com.kwp.chat.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 好友控制器
 */
@Tag(name = "好友管理", description = "好友申请、好友列表等接口")
@RestController
@RequestMapping("/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "发送好友申请")
    @PostMapping("/request")
    public Result<Void> sendFriendRequest(HttpServletRequest request,
                                          @RequestParam Long receiverId,
                                          @RequestParam(required = false) String message) {
        Long userId = getCurrentUserId(request);
        friendService.sendFriendRequest(userId, receiverId, message);
        return Result.success();
    }

    @Operation(summary = "处理好友申请")
    @PutMapping("/request/{requestId}")
    public Result<Void> handleFriendRequest(HttpServletRequest request,
                                            @PathVariable Long requestId,
                                            @RequestBody Map<String, Integer> body) {
        Long userId = getCurrentUserId(request);
        Integer status = body.get("status");
        friendService.handleFriendRequest(requestId, userId, status);
        return Result.success();
    }

    @Operation(summary = "获取好友列表")
    @GetMapping("/list")
    public Result<List<UserInfoResponse>> getFriendList(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<UserInfoResponse> friends = friendService.getFriendList(userId);
        return Result.success(friends);
    }

    @Operation(summary = "获取收到的好友申请")
    @GetMapping("/requests/received")
    public Result<List<FriendRequest>> getReceivedFriendRequests(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<FriendRequest> requests = friendService.getReceivedFriendRequests(userId);
        return Result.success(requests);
    }

    @Operation(summary = "获取发出的好友申请")
    @GetMapping("/requests/sent")
    public Result<List<FriendRequest>> getSentFriendRequests(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<FriendRequest> requests = friendService.getSentFriendRequests(userId);
        return Result.success(requests);
    }

    @Operation(summary = "删除好友")
    @DeleteMapping("/{friendId}")
    public Result<Void> deleteFriend(HttpServletRequest request, @PathVariable Long friendId) {
        Long userId = getCurrentUserId(request);
        friendService.deleteFriend(userId, friendId);
        return Result.success();
    }

    @Operation(summary = "更新好友备注")
    @PutMapping("/{friendId}/remark")
    public Result<Void> updateFriendRemark(HttpServletRequest request,
                                           @PathVariable Long friendId,
                                           @RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId(request);
        String remark = body.get("remark");
        friendService.updateFriendRemark(userId, friendId, remark);
        return Result.success();
    }

    @Operation(summary = "检查是否是好友")
    @GetMapping("/{friendId}/check")
    public Result<Boolean> checkIsFriend(HttpServletRequest request, @PathVariable Long friendId) {
        Long userId = getCurrentUserId(request);
        boolean isFriend = friendService.isFriend(userId, friendId);
        return Result.success(isFriend);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        return userId;
    }
}