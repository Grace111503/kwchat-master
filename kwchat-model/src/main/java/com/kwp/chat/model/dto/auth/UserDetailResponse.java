package com.kwp.chat.model.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 用户详情响应DTO（包含角色权限信息）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 角色列表
     */
    private List<RoleInfo> roles;

    /**
     * 角色编码集合
     */
    private Set<String> roleCodes;

    /**
     * 权限编码集合
     */
    private Set<String> permissions;

    /**
     * 是否管理员
     */
    private Boolean isAdmin;

    /**
     * 是否老板
     */
    private Boolean isBoss;

    /**
     * 是否部门经理
     */
    private Boolean isManager;

    /**
     * 角色信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleInfo implements Serializable {
        private Long roleId;
        private String roleCode;
        private String roleName;
    }
}