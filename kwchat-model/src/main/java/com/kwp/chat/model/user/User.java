package com.kwp.chat.model.user;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kwp.chat.model.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 性别（1：男，2：女）
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 生日
     */
    @TableField("birthday")
    private LocalDateTime birthday;

    /**
     * 个性签名
     */
    @TableField("signature")
    private String signature;

    /**
     * 部门
     */
    @TableField("department")
    private String department;

    /**
     * 状态（0：禁用，1：启用）
     */
    @TableField("status")
    private Integer status;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 在线状态（0：离线，1：在线，2：忙碌，3：离开）
     */
    @TableField("online_status")
    private Integer onlineStatus;

    /**
     * 用户类型（0：普通用户，1：管理员，2：超级管理员）
     */
    @TableField("user_type")
    private Integer userType;

    /**
     * 部门ID
     */
    @TableField("dept_id")
    private Long deptId;
}