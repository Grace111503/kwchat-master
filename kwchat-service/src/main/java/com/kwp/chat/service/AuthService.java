package com.kwp.chat.service;

import com.kwp.chat.model.auth.Department;
import com.kwp.chat.model.auth.Permission;
import com.kwp.chat.model.auth.Role;
import com.kwp.chat.model.dto.auth.DepartmentDTO;
import com.kwp.chat.model.dto.auth.RoleDTO;
import com.kwp.chat.model.dto.auth.UserDetailResponse;

import java.util.List;
import java.util.Set;

/**
 * 权限服务接口
 */
public interface AuthService {

    // ========== 用户权限 ==========

    /**
     * 获取用户详情（包含角色权限）
     */
    UserDetailResponse getUserDetail(Long userId);

    /**
     * 获取用户角色列表
     */
    List<Role> getUserRoles(Long userId);

    /**
     * 获取用户权限集合
     */
    Set<String> getUserPermissions(Long userId);

    /**
     * 检查用户是否有指定角色
     */
    boolean hasRole(Long userId, String roleCode);

    /**
     * 检查用户是否有任意一个角色
     */
    boolean hasAnyRole(Long userId, String... roleCodes);

    /**
     * 检查用户是否有指定权限
     */
    boolean hasPermission(Long userId, String permissionCode);

    /**
     * 检查用户是否有任意一个权限
     */
    boolean hasAnyPermission(Long userId, String... permissionCodes);

    /**
     * 分配用户角色
     */
    void assignUserRoles(Long userId, List<Long> roleIds);

    // ========== 角色管理 ==========

    /**
     * 获取所有角色
     */
    List<Role> getAllRoles();

    /**
     * 创建角色
     */
    void createRole(RoleDTO roleDTO);

    /**
     * 更新角色
     */
    void updateRole(Long roleId, RoleDTO roleDTO);

    /**
     * 删除角色
     */
    void deleteRole(Long roleId);

    /**
     * 获取角色的权限ID列表
     */
    Set<Long> getRolePermissionIds(Long roleId);

    /**
     * 分配角色权限
     */
    void assignRolePermissions(Long roleId, List<Long> permissionIds);

    // ========== 权限管理 ==========

    /**
     * 获取所有权限
     */
    List<Permission> getAllPermissions();

    /**
     * 获取权限树
     */
    List<Permission> getPermissionTree();

    // ========== 部门管理 ==========

    /**
     * 获取所有部门
     */
    List<Department> getAllDepartments();

    /**
     * 获取部门树
     */
    List<Department> getDepartmentTree();

    /**
     * 创建部门
     */
    void createDepartment(DepartmentDTO deptDTO);

    /**
     * 更新部门
     */
    void updateDepartment(Long deptId, DepartmentDTO deptDTO);

    /**
     * 删除部门
     */
    void deleteDepartment(Long deptId);
}