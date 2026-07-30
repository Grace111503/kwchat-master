package com.kwp.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kwp.chat.common.constant.CommonConstant;
import com.kwp.chat.common.exception.BusinessException;
import com.kwp.chat.common.result.ResultCode;
import com.kwp.chat.common.utils.JwtUtils;
import com.kwp.chat.common.utils.LocalStorageUtils;
import com.kwp.chat.common.utils.MinioUtils;
import com.kwp.chat.common.utils.RedisUtils;
import com.kwp.chat.dao.UserMapper;
import com.kwp.chat.model.dto.*;
import com.kwp.chat.model.user.User;
import com.kwp.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisUtils redisUtils;
    private final LocalStorageUtils localStorageUtils;
    private final MinioUtils minioUtils;

    @Value("${file.storage.type:minio}")
    private String storageType;
    private final MinioUtils minioUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(RegisterRequest request) {
        // 验证密码一致性
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "两次密码不一致");
        }

        // 检查用户名是否存在
        if (userMapper.countByUsername(request.getUsername()) > 0) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS, "用户名已存在");
        }

        // 检查手机号是否存在
        if (StringUtils.hasText(request.getPhone()) && userMapper.countByPhone(request.getPhone()) > 0) {
            throw new BusinessException(ResultCode.PHONE_ALREADY_EXISTS, "手机号已注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getUsername()); // 使用用户名作为昵称
        user.setDepartment(request.getDepartment());
        user.setPhone(request.getPhone());
        user.setStatus(CommonConstant.STATUS_ENABLE);
        user.setGender(1); // 默认性别为男
        user.setOnlineStatus(0);
        user.setUserType(0);

        userMapper.insert(user);

        // 生成Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername());

        // 存储Token到Redis
        redisUtils.setEx(CommonConstant.REDIS_TOKEN_PREFIX + user.getId(), token, 86400);

        log.info("用户注册成功: {}", user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(86400L)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        // 检查用户状态
        if (CommonConstant.STATUS_DISABLE.equals(user.getStatus())) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 生成Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername());

        // 存储Token到Redis
        redisUtils.setEx(CommonConstant.REDIS_TOKEN_PREFIX + user.getId(), token, 86400);

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 更新在线状态
        redisUtils.setEx(CommonConstant.REDIS_ONLINE_PREFIX + user.getId(), 1, 86400);

        log.info("用户登录成功: {}", user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(86400L)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public void logout(String token) {
        try {
            Long userId = jwtUtils.getUserId(token);
            // 删除Redis中的Token
            redisUtils.delete(CommonConstant.REDIS_TOKEN_PREFIX + userId);
            // 更新在线状态
            redisUtils.delete(CommonConstant.REDIS_ONLINE_PREFIX + userId);

            log.info("用户登出成功: userId={}", userId);
        } catch (Exception e) {
            log.error("登出失败: {}", e.getMessage());
        }
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        return convertToUserInfoResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResponse updateUserInfo(Long userId, UpdateUserRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 检查昵称是否重复
        if (StringUtils.hasText(request.getNickname()) && !request.getNickname().equals(user.getNickname())) {
            // 这里可以添加昵称重复检查逻辑
            user.setNickname(request.getNickname());
        }

        // 更新字段
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday().atStartOfDay());
        }
        if (request.getSignature() != null) {
            user.setSignature(request.getSignature());
        }
        if (StringUtils.hasText(request.getDepartment())) {
            user.setDepartment(request.getDepartment());
        }

        userMapper.updateById(user);

        return convertToUserInfoResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }

        // 验证新密码一致性
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "两次密码不一致");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);

        // 删除旧Token，强制重新登录
        redisUtils.delete(CommonConstant.REDIS_TOKEN_PREFIX + userId);

        log.info("用户修改密码成功: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(Long userId, MultipartFile file) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        try {
            // 生成唯一文件名
            String fileName = "avatar/" + UUID.randomUUID().toString().replace("-", "") + getFileExtension(file.getOriginalFilename());

            String avatarUrl;
            if ("local".equals(storageType)) {
                // 使用本地存储
                avatarUrl = localStorageUtils.uploadFile(file.getInputStream(), fileName, file.getContentType(), file.getSize());
            } else {
                // 使用 MinIO 存储
                avatarUrl = minioUtils.uploadFile(file.getInputStream(), fileName, file.getContentType(), file.getSize());
            }

            // 更新用户头像URL
            user.setAvatar(avatarUrl);
            userMapper.updateById(user);

            log.info("用户头像上传成功: userId={}, avatarUrl={}", userId, avatarUrl);
            return avatarUrl;
        } catch (Exception e) {
            log.error("头像上传失败: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED, "头像上传失败");
        }
    }

    @Override
    public List<UserInfoResponse> searchUser(String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .like(User::getUsername, keyword)
                .or().like(User::getNickname, keyword)
                .or().like(User::getPhone, keyword)
                .or().like(User::getEmail, keyword)
        );
        wrapper.eq(User::getStatus, CommonConstant.STATUS_ENABLE);
        wrapper.last("LIMIT 20");

        List<User> users = userMapper.selectList(wrapper);
        return users.stream()
                .map(this::convertToUserInfoResponse)
                .collect(Collectors.toList());
    }

    @Override
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        try {
            // 验证刷新Token
            if (!jwtUtils.validateToken(refreshToken)) {
                throw new BusinessException(ResultCode.TOKEN_INVALID);
            }

            Long userId = jwtUtils.getUserId(refreshToken);
            String username = jwtUtils.getUsername(refreshToken);

            // 生成新的Token
            String newToken = jwtUtils.generateToken(userId, username);
            String newRefreshToken = jwtUtils.generateRefreshToken(userId, username);

            // 存储新Token到Redis
            redisUtils.setEx(CommonConstant.REDIS_TOKEN_PREFIX + userId, newToken, 86400);

            User user = userMapper.selectById(userId);

            return LoginResponse.builder()
                    .token(newToken)
                    .refreshToken(newRefreshToken)
                    .expiresIn(86400L)
                    .userId(userId)
                    .username(username)
                    .nickname(user != null ? user.getNickname() : null)
                    .avatar(user != null ? user.getAvatar() : null)
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
    }

    /**
     * 转换为UserInfoResponse
     */
    private UserInfoResponse convertToUserInfoResponse(User user) {
        UserInfoResponse response = new UserInfoResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            return fileName.substring(lastDotIndex);
        }
        return "";
    }
}