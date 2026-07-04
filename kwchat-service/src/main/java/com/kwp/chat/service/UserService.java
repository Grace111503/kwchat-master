package com.kwp.chat.service;

import com.kwp.chat.model.dto.*;
import com.kwp.chat.model.user.User;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    LoginResponse register(RegisterRequest request);

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户登出
     */
    void logout(String token);

    /**
     * 获取用户信息
     */
    UserInfoResponse getUserInfo(Long userId);

    /**
     * 更新用户信息
     */
    UserInfoResponse updateUserInfo(Long userId, UpdateUserRequest request);

    /**
     * 修改密码
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * 上传头像
     */
    String uploadAvatar(Long userId, MultipartFile file);

    /**
     * 搜索用户
     */
    java.util.List<UserInfoResponse> searchUser(String keyword);

    /**
     * 根据ID获取用户
     */
    User getUserById(Long userId);

    /**
     * 根据用户名获取用户
     */
    User getUserByUsername(String username);

    /**
     * 刷新Token
     */
    LoginResponse refreshToken(String refreshToken);
}