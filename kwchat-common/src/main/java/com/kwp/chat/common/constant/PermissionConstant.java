package com.kwp.chat.common.constant;

/**
 * 权限常量
 */
public class PermissionConstant {

    // ========== 角色编码 ==========

    /**
     * 超级管理员
     */
    public static final String ROLE_ADMIN = "admin";

    /**
     * 老板
     */
    public static final String ROLE_BOSS = "boss";

    /**
     * 部门经理
     */
    public static final String ROLE_MANAGER = "manager";

    /**
     * 部门成员
     */
    public static final String ROLE_MEMBER = "member";

    // ========== 权限编码 ==========

    // 用户管理
    public static final String PERM_USER_VIEW = "user:view";
    public static final String PERM_USER_CREATE = "user:create";
    public static final String PERM_USER_EDIT = "user:edit";
    public static final String PERM_USER_DELETE = "user:delete";
    public static final String PERM_USER_EXPORT = "user:export";

    // 部门管理
    public static final String PERM_DEPT_VIEW = "dept:view";
    public static final String PERM_DEPT_CREATE = "dept:create";
    public static final String PERM_DEPT_EDIT = "dept:edit";
    public static final String PERM_DEPT_DELETE = "dept:delete";

    // 角色管理
    public static final String PERM_ROLE_VIEW = "role:view";
    public static final String PERM_ROLE_CREATE = "role:create";
    public static final String PERM_ROLE_EDIT = "role:edit";
    public static final String PERM_ROLE_DELETE = "role:delete";

    // 群组管理
    public static final String PERM_GROUP_VIEW = "group:view";
    public static final String PERM_GROUP_CREATE = "group:create";
    public static final String PERM_GROUP_EDIT = "group:edit";
    public static final String PERM_GROUP_DELETE = "group:delete";
    public static final String PERM_GROUP_MANAGE = "group:manage";

    // 消息管理
    public static final String PERM_MSG_VIEW = "msg:view";
    public static final String PERM_MSG_DELETE = "msg:delete";
    public static final String PERM_MSG_RECALL = "msg:recall";

    // 文件管理
    public static final String PERM_FILE_VIEW = "file:view";
    public static final String PERM_FILE_DELETE = "file:delete";

    // 系统设置
    public static final String PERM_SYSTEM_CONFIG = "system:config";
    public static final String PERM_SYSTEM_LOG = "system:log";

    // AI功能
    public static final String PERM_AI_CONFIG = "ai:config";
    public static final String PERM_AI_USE = "ai:use";

    // 数据统计
    public static final String PERM_STATS_VIEW = "stats:view";
    public static final String PERM_STATS_EXPORT = "stats:export";
}