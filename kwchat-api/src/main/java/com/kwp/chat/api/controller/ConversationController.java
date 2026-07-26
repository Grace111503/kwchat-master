package com.kwp.chat.api.controller;

import com.kwp.chat.common.result.Result;
import com.kwp.chat.model.message.Conversation;
import com.kwp.chat.model.message.ConversationMember;
import com.kwp.chat.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 会话控制器
 */
@Tag(name = "会话管理", description = "会话列表、创建会话等接口")
@RestController
@RequestMapping("/conversation")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @Operation(summary = "获取会话列表")
    @GetMapping("/list")
    public Result<List<Conversation>> getConversationList(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<Conversation> conversations = conversationService.getConversationList(userId);
        return Result.success(conversations);
    }

    @Operation(summary = "获取或创建单聊会话")
    @PostMapping("/private")
    public Result<Conversation> getOrCreatePrivateConversation(HttpServletRequest request,
                                                               @RequestParam Long targetUserId) {
        Long userId = getCurrentUserId(request);
        Conversation conversation = conversationService.getOrCreatePrivateConversation(userId, targetUserId);
        return Result.success(conversation);
    }

    @Operation(summary = "创建群聊会话")
    @PostMapping("/group")
    public Result<Conversation> createGroupConversation(HttpServletRequest request,
                                                       @RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId(request);
        String name = (String) body.get("name");
        @SuppressWarnings("unchecked")
        List<Object> rawMemberIds = (List<Object>) body.get("memberIds");
        List<Long> memberIds = rawMemberIds != null
                ? rawMemberIds.stream().map(id -> Long.valueOf(id.toString())).toList()
                : List.of();
        Conversation conversation = conversationService.createGroupConversation(userId, name, memberIds);
        return Result.success(conversation);
    }

    @Operation(summary = "获取会话详情")
    @GetMapping("/{conversationId}")
    public Result<Conversation> getConversation(@PathVariable Long conversationId) {
        Conversation conversation = conversationService.getConversation(conversationId);
        return Result.success(conversation);
    }

    @Operation(summary = "获取会话成员列表")
    @GetMapping("/{conversationId}/members")
    public Result<List<ConversationMember>> getConversationMembers(@PathVariable Long conversationId) {
        List<ConversationMember> members = conversationService.getConversationMembers(conversationId);
        return Result.success(members);
    }

    @Operation(summary = "添加会话成员")
    @PostMapping("/{conversationId}/members")
    public Result<Void> addConversationMember(@PathVariable Long conversationId,
                                              @RequestParam Long userId) {
        conversationService.addConversationMember(conversationId, userId);
        return Result.success();
    }

    @Operation(summary = "移除会话成员")
    @DeleteMapping("/{conversationId}/members/{userId}")
    public Result<Void> removeConversationMember(@PathVariable Long conversationId,
                                                 @PathVariable Long userId) {
        conversationService.removeConversationMember(conversationId, userId);
        return Result.success();
    }

    @Operation(summary = "清除未读消息数")
    @PutMapping("/{conversationId}/read")
    public Result<Void> clearUnreadCount(HttpServletRequest request,
                                         @PathVariable Long conversationId) {
        Long userId = getCurrentUserId(request);
        conversationService.clearUnreadCount(conversationId, userId);
        return Result.success();
    }

    @Operation(summary = "设置免打扰")
    @PutMapping("/{conversationId}/disturb")
    public Result<Void> setDoNotDisturb(HttpServletRequest request,
                                        @PathVariable Long conversationId,
                                        @RequestBody Map<String, Integer> body) {
        Long userId = getCurrentUserId(request);
        Integer doNotDisturb = body.get("doNotDisturb");
        conversationService.setDoNotDisturb(conversationId, userId, doNotDisturb);
        return Result.success();
    }

    @Operation(summary = "设置置顶")
    @PutMapping("/{conversationId}/top")
    public Result<Void> setTop(HttpServletRequest request,
                               @PathVariable Long conversationId,
                               @RequestBody Map<String, Integer> body) {
        Long userId = getCurrentUserId(request);
        Integer isTop = body.get("isTop");
        conversationService.setTop(conversationId, userId, isTop);
        return Result.success();
    }

    @Operation(summary = "更新群公告")
    @PutMapping("/{conversationId}/announcement")
    public Result<Void> updateAnnouncement(@PathVariable Long conversationId,
                                           @RequestBody Map<String, String> body) {
        String announcement = body.get("announcement");
        conversationService.updateAnnouncement(conversationId, announcement);
        return Result.success();
    }

    @Operation(summary = "更新群名称")
    @PutMapping("/{conversationId}/name")
    public Result<Void> updateGroupName(@PathVariable Long conversationId,
                                        @RequestBody Map<String, String> body) {
        String name = body.get("name");
        conversationService.updateGroupName(conversationId, name);
        return Result.success();
    }

    @Operation(summary = "更新群头像")
    @PutMapping("/{conversationId}/avatar")
    public Result<Void> updateGroupAvatar(@PathVariable Long conversationId,
                                          @RequestBody Map<String, String> body) {
        String avatar = body.get("avatar");
        conversationService.updateGroupAvatar(conversationId, avatar);
        return Result.success();
    }

    @Operation(summary = "解散群聊")
    @DeleteMapping("/{conversationId}")
    public Result<Void> dissolveGroup(HttpServletRequest request,
                                      @PathVariable Long conversationId) {
        Long userId = getCurrentUserId(request);
        conversationService.dissolveGroup(conversationId, userId);
        return Result.success();
    }

    @Operation(summary = "更新成员角色")
    @PutMapping("/{conversationId}/members/{userId}/role")
    public Result<Void> updateMemberRole(HttpServletRequest request,
                                         @PathVariable Long conversationId,
                                         @PathVariable Long userId,
                                         @RequestBody Map<String, Integer> body) {
        Long currentUserId = getCurrentUserId(request);
        Integer role = body.get("role");
        conversationService.updateMemberRole(conversationId, userId, role);
        return Result.success();
    }

    @Operation(summary = "退出会话")
    @PostMapping("/{conversationId}/exit")
    public Result<Void> exitConversation(HttpServletRequest request,
                                         @PathVariable Long conversationId) {
        Long userId = getCurrentUserId(request);
        conversationService.exitConversation(conversationId, userId);
        return Result.success();
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