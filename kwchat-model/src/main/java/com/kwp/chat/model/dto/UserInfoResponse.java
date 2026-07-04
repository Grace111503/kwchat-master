package com.kwp.chat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别（1：男，2：女）
     */
    private Integer gender;

    /**
     * 生日
     */
    private LocalDateTime birthday;

    /**
     * 个性签名
     */
    private String signature;

    /**
     * 部门
     */
    private String department;

    /**
     * 在线状态（0：离线，1：在线，2：忙碌，3：离开）
     */
    private Integer onlineStatus;

    /**
     * 用户类型（0：普通用户，1：管理员，2：超级管理员）
     */
    private Integer userType;

    /**
     * 注册时间
     */
    private LocalDateTime createTime;
}