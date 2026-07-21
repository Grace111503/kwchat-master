package com.kwp.chat.api.controller;

import com.kwp.chat.common.result.Result;
import com.kwp.chat.model.message.Message;
import com.kwp.chat.service.MessageFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息收藏控制器
 */
@Tag(name = "消息收藏", description = "消息收藏相关接口")
@RestController
@RequestMapping("/message/favorite")
@RequiredArgsConstructor
public class MessageFavoriteController {

    private final MessageFavoriteService messageFavoriteService;

    @Operation(summary = "收藏消息")
    @PostMapping("/{messageId}")
    public Result<Void> favoriteMessage(HttpServletRequest request,
                                        @PathVariable Long messageId) {
        Long userId = getCurrentUserId(request);
        messageFavoriteService.favoriteMessage(messageId, userId);
        return Result.success();
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{messageId}")
    public Result<Void> unfavoriteMessage(HttpServletRequest request,
                                          @PathVariable Long messageId) {
        Long userId = getCurrentUserId(request);
        messageFavoriteService.unfavoriteMessage(messageId, userId);
        return Result.success();
    }

    @Operation(summary = "检查是否已收藏")
    @GetMapping("/{messageId}/check")
    public Result<Boolean> isFavorited(HttpServletRequest request,
                                       @PathVariable Long messageId) {
        Long userId = getCurrentUserId(request);
        boolean isFavorited = messageFavoriteService.isFavorited(messageId, userId);
        return Result.success(isFavorited);
    }

    @Operation(summary = "获取收藏列表")
    @GetMapping("/list")
    public Result<List<Message>> getFavoritedMessages(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<Message> messages = messageFavoriteService.getFavoritedMessages(userId);
        return Result.success(messages);
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
