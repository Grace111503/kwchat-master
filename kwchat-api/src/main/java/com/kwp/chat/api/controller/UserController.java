package com.kwp.chat.api.controller;

import com.kwp.chat.common.result.Result;
import com.kwp.chat.model.dto.*;
import com.kwp.chat.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户注册、登录、信息管理等接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = userService.register(request);
        return Result.success(response);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success(response);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null) {
            userService.logout(token);
        }
        return Result.success();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<UserInfoResponse> getUserInfo(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        UserInfoResponse response = userService.getUserInfo(userId);
        return Result.success(response);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/info")
    public Result<UserInfoResponse> updateUserInfo(HttpServletRequest request, @Valid @RequestBody UpdateUserRequest updateRequest) {
        Long userId = getCurrentUserId(request);
        UserInfoResponse response = userService.updateUserInfo(userId, updateRequest);
        return Result.success(response);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(HttpServletRequest request, @Valid @RequestBody ChangePasswordRequest changeRequest) {
        Long userId = getCurrentUserId(request);
        userService.changePassword(userId, changeRequest);
        return Result.success();
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        Long userId = getCurrentUserId(request);
        String url = userService.uploadAvatar(userId, file);
        return Result.success(url);
    }

    @Operation(summary = "搜索用户")
    @GetMapping("/search")
    public Result<List<UserInfoResponse>> searchUser(@RequestParam("keyword") String keyword) {
        List<UserInfoResponse> response = userService.searchUser(keyword);
        return Result.success(response);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{userId}")
    public Result<UserInfoResponse> getUserDetail(@PathVariable Long userId) {
        UserInfoResponse response = userService.getUserInfo(userId);
        return Result.success(response);
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        LoginResponse response = userService.refreshToken(refreshToken);
        return Result.success(response);
    }

    /**
     * 从请求中提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
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