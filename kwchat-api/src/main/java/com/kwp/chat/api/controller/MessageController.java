package com.kwp.chat.api.controller;

import com.kwp.chat.common.result.Result;
import com.kwp.chat.model.message.Message;
import com.kwp.chat.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 消息控制器
 */
@Tag(name = "消息管理", description = "消息发送、接收、撤回等接口")
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "发送消息")
    @PostMapping("/send")
    public Result<Message> sendMessage(HttpServletRequest request,
                                       @RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId(request);

        Long conversationId = Long.valueOf(body.get("conversationId").toString());
        Integer messageType = body.get("messageType") != null ? ((Number) body.get("messageType")).intValue() : null;
        String content = (String) body.get("content");
        String fileUrl = (String) body.get("fileUrl");
        String fileName = (String) body.get("fileName");
        Long fileSize = body.get("fileSize") != null ? Long.valueOf(body.get("fileSize").toString()) : null;
        String fileType = (String) body.get("fileType");
        String thumbnailUrl = (String) body.get("thumbnailUrl");
        Integer duration = body.get("duration") != null ? ((Number) body.get("duration")).intValue() : null;
        Long replyMessageId = body.get("replyMessageId") != null ? Long.valueOf(body.get("replyMessageId").toString()) : null;
        String atUserIds = (String) body.get("atUserIds");
        String clientMessageId = (String) body.get("clientMessageId");

        Message message = messageService.sendMessage(conversationId, userId, messageType, content,
                fileUrl, fileName, fileSize, fileType, thumbnailUrl, duration,
                replyMessageId, atUserIds, clientMessageId);

        return Result.success(message);
    }

    @Operation(summary = "获取会话消息列表")
    @GetMapping("/list")
    public Result<List<Message>> getMessages(HttpServletRequest request,
                                             @RequestParam Long conversationId,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        Long userId = getCurrentUserId(request);
        List<Message> messages = messageService.getMessages(conversationId, userId, page, size);
        return Result.success(messages);
    }

    @Operation(summary = "获取消息详情")
    @GetMapping("/{messageId}")
    public Result<Message> getMessage(@PathVariable Long messageId) {
        Message message = messageService.getMessage(messageId);
        return Result.success(message);
    }

    @Operation(summary = "撤回消息")
    @PostMapping("/{messageId}/recall")
    public Result<Void> recallMessage(HttpServletRequest request, @PathVariable Long messageId) {
        Long userId = getCurrentUserId(request);
        messageService.recallMessage(messageId, userId);
        return Result.success();
    }

    @Operation(summary = "删除消息")
    @DeleteMapping("/{messageId}")
    public Result<Void> deleteMessage(HttpServletRequest request, @PathVariable Long messageId) {
        Long userId = getCurrentUserId(request);
        messageService.deleteMessage(messageId, userId);
        return Result.success();
    }

    @Operation(summary = "标记消息已读")
    @PostMapping("/{messageId}/read")
    public Result<Void> markMessageAsRead(HttpServletRequest request, @PathVariable Long messageId) {
        Long userId = getCurrentUserId(request);
        messageService.markMessageAsRead(messageId, userId);
        return Result.success();
    }

    @Operation(summary = "标记会话消息已读")
    @PostMapping("/conversation/{conversationId}/read")
    public Result<Void> markConversationMessagesAsRead(HttpServletRequest request,
                                                       @PathVariable Long conversationId) {
        Long userId = getCurrentUserId(request);
        messageService.markConversationMessagesAsRead(conversationId, userId);
        return Result.success();
    }

    @Operation(summary = "获取未读消息数")
    @GetMapping("/conversation/{conversationId}/unread")
    public Result<Integer> getUnreadMessageCount(HttpServletRequest request,
                                                 @PathVariable Long conversationId) {
        Long userId = getCurrentUserId(request);
        int count = messageService.getUnreadMessageCount(conversationId, userId);
        return Result.success(count);
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