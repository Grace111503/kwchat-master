package com.kwp.chat.api.controller;

import com.kwp.chat.common.result.Result;
import com.kwp.chat.model.auth.Department;
import com.kwp.chat.model.auth.Permission;
import com.kwp.chat.model.auth.Role;
import com.kwp.chat.model.dto.auth.DepartmentDTO;
import com.kwp.chat.model.dto.auth.RoleDTO;
import com.kwp.chat.model.dto.auth.UserDetailResponse;
import com.kwp.chat.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 权限管理控制器
 */
@Tag(name = "权限管理", description = "角色、权限、部门管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ========== 用户权限 ==========

    @Operation(summary = "获取当前用户详情（含角色权限）")
    @GetMapping("/user/detail")
    public Result<UserDetailResponse> getCurrentUserDetail(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        UserDetailResponse detail = authService.getUserDetail(userId);
        return Result.success(detail);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/user/{userId}/detail")
    public Result<UserDetailResponse> getUserDetail(@PathVariable Long userId) {
        UserDetailResponse detail = authService.getUserDetail(userId);
        return Result.success(detail);
    }

    @Operation(summary = "分配用户角色")
    @PostMapping("/user/{userId}/roles")
    public Result<Void> assignUserRoles(@PathVariable Long userId, @RequestBody Map<String, List<Long>> body) {
        List<Long> roleIds = body.get("roleIds");
        authService.assignUserRoles(userId, roleIds);
        return Result.success();
    }

    // ========== 角色管理 ==========

    @Operation(summary = "获取所有角色")
    @GetMapping("/roles")
    public Result<List<Role>> getAllRoles() {
        List<Role> roles = authService.getAllRoles();
        return Result.success(roles);
    }

    @Operation(summary = "创建角色")
    @PostMapping("/role")
    public Result<Void> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        authService.createRole(roleDTO);
        return Result.success();
    }

    @Operation(summary = "更新角色")
    @PutMapping("/role/{roleId}")
    public Result<Void> updateRole(@PathVariable Long roleId, @Valid @RequestBody RoleDTO roleDTO) {
        authService.updateRole(roleId, roleDTO);
        return Result.success();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/role/{roleId}")
    public Result<Void> deleteRole(@PathVariable Long roleId) {
        authService.deleteRole(roleId);
        return Result.success();
    }

    @Operation(summary = "获取角色的权限ID列表")
    @GetMapping("/role/{roleId}/permissions")
    public Result<Set<Long>> getRolePermissionIds(@PathVariable Long roleId) {
        Set<Long> permissionIds = authService.getRolePermissionIds(roleId);
        return Result.success(permissionIds);
    }

    @Operation(summary = "分配角色权限")
    @PostMapping("/role/{roleId}/permissions")
    public Result<Void> assignRolePermissions(@PathVariable Long roleId, @RequestBody Map<String, List<Long>> body) {
        List<Long> permissionIds = body.get("permissionIds");
        authService.assignRolePermissions(roleId, permissionIds);
        return Result.success();
    }

    // ========== 权限管理 ==========

    @Operation(summary = "获取所有权限")
    @GetMapping("/permissions")
    public Result<List<Permission>> getAllPermissions() {
        List<Permission> permissions = authService.getAllPermissions();
        return Result.success(permissions);
    }

    @Operation(summary = "获取权限树")
    @GetMapping("/permissions/tree")
    public Result<List<Permission>> getPermissionTree() {
        List<Permission> permissions = authService.getPermissionTree();
        return Result.success(permissions);
    }

    // ========== 部门管理 ==========

    @Operation(summary = "获取所有部门")
    @GetMapping("/departments")
    public Result<List<Department>> getAllDepartments() {
        List<Department> departments = authService.getAllDepartments();
        return Result.success(departments);
    }

    @Operation(summary = "获取部门树")
    @GetMapping("/departments/tree")
    public Result<List<Department>> getDepartmentTree() {
        List<Department> departments = authService.getDepartmentTree();
        return Result.success(departments);
    }

    @Operation(summary = "创建部门")
    @PostMapping("/department")
    public Result<Void> createDepartment(@Valid @RequestBody DepartmentDTO deptDTO) {
        authService.createDepartment(deptDTO);
        return Result.success();
    }

    @Operation(summary = "更新部门")
    @PutMapping("/department/{deptId}")
    public Result<Void> updateDepartment(@PathVariable Long deptId, @Valid @RequestBody DepartmentDTO deptDTO) {
        authService.updateDepartment(deptId, deptDTO);
        return Result.success();
    }

    @Operation(summary = "删除部门")
    @DeleteMapping("/department/{deptId}")
    public Result<Void> deleteDepartment(@PathVariable Long deptId) {
        authService.deleteDepartment(deptId);
        return Result.success();
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