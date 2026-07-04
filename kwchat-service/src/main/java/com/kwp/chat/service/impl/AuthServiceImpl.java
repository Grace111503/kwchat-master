package com.kwp.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kwp.chat.common.constant.PermissionConstant;
import com.kwp.chat.common.exception.BusinessException;
import com.kwp.chat.common.result.ResultCode;
import com.kwp.chat.dao.*;
import com.kwp.chat.model.auth.*;
import com.kwp.chat.model.dto.auth.DepartmentDTO;
import com.kwp.chat.model.dto.auth.RoleDTO;
import com.kwp.chat.model.dto.auth.UserDetailResponse;
import com.kwp.chat.model.user.User;
import com.kwp.chat.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final DepartmentMapper departmentMapper;
    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public UserDetailResponse getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 获取用户角色
        List<Role> roles = roleMapper.selectRolesByUserId(userId);
        Set<String> roleCodes = roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toSet());

        // 获取用户权限
        Set<String> permissions = permissionMapper.selectPermissionCodesByUserId(userId);

        // 获取部门信息
        String deptName = null;
        if (user.getDeptId() != null) {
            Department dept = departmentMapper.selectById(user.getDeptId());
            deptName = dept != null ? dept.getDeptName() : null;
        }

        // 构建角色信息列表
        List<UserDetailResponse.RoleInfo> roleInfos = roles.stream()
                .map(role -> UserDetailResponse.RoleInfo.builder()
                        .roleId(role.getId())
                        .roleCode(role.getRoleCode())
                        .roleName(role.getRoleName())
                        .build())
                .collect(Collectors.toList());

        return UserDetailResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .deptId(user.getDeptId())
                .deptName(deptName)
                .roles(roleInfos)
                .roleCodes(roleCodes)
                .permissions(permissions)
                .isAdmin(roleCodes.contains(PermissionConstant.ROLE_ADMIN))
                .isBoss(roleCodes.contains(PermissionConstant.ROLE_BOSS))
                .isManager(roleCodes.contains(PermissionConstant.ROLE_MANAGER))
                .build();
    }

    @Override
    public List<Role> getUserRoles(Long userId) {
        return roleMapper.selectRolesByUserId(userId);
    }

    @Override
    public Set<String> getUserPermissions(Long userId) {
        return permissionMapper.selectPermissionCodesByUserId(userId);
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        List<Role> roles = roleMapper.selectRolesByUserId(userId);
        return roles.stream().anyMatch(r -> r.getRoleCode().equals(roleCode));
    }

    @Override
    public boolean hasAnyRole(Long userId, String... roleCodes) {
        if (roleCodes == null || roleCodes.length == 0) {
            return false;
        }
        List<Role> roles = roleMapper.selectRolesByUserId(userId);
        Set<String> userRoleCodes = roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toSet());

        for (String roleCode : roleCodes) {
            if (userRoleCodes.contains(roleCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        Set<String> permissions = permissionMapper.selectPermissionCodesByUserId(userId);
        return permissions.contains(permissionCode);
    }

    @Override
    public boolean hasAnyPermission(Long userId, String... permissionCodes) {
        if (permissionCodes == null || permissionCodes.length == 0) {
            return false;
        }
        Set<String> permissions = permissionMapper.selectPermissionCodesByUserId(userId);

        for (String permissionCode : permissionCodes) {
            if (permissions.contains(permissionCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        // 删除原有角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId));

        // 添加新的角色关联
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }

        log.info("分配用户角色: userId={}, roleIds={}", userId, roleIds);
    }

    @Override
    public List<Role> getAllRoles() {
        return roleMapper.selectList(new LambdaQueryWrapper<Role>()
                .eq(Role::getDeleted, 0)
                .orderByAsc(Role::getSortOrder));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRole(RoleDTO roleDTO) {
        // 检查角色编码是否重复
        Role existing = roleMapper.selectByRoleCode(roleDTO.getRoleCode());
        if (existing != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "角色编码已存在");
        }

        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);
        role.setStatus(1);
        role.setIsSystem(0);
        roleMapper.insert(role);

        // 分配权限
        if (!CollectionUtils.isEmpty(roleDTO.getPermissionIds())) {
            assignRolePermissions(role.getId(), roleDTO.getPermissionIds());
        }

        log.info("创建角色: {}", roleDTO.getRoleCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long roleId, RoleDTO roleDTO) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "角色不存在");
        }

        if (role.getIsSystem() == 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "系统内置角色不能修改");
        }

        BeanUtils.copyProperties(roleDTO, role);
        roleMapper.updateById(role);

        // 更新权限
        if (roleDTO.getPermissionIds() != null) {
            assignRolePermissions(roleId, roleDTO.getPermissionIds());
        }

        log.info("更新角色: roleId={}", roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "角色不存在");
        }

        if (role.getIsSystem() == 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "系统内置角色不能删除");
        }

        // 检查是否有用户使用该角色
        Set<Long> userIds = userRoleMapper.selectUserIdsByRoleId(roleId);
        if (!userIds.isEmpty()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "该角色下还有用户，不能删除");
        }

        roleMapper.deleteById(roleId);

        // 删除角色权限关联
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, roleId));

        log.info("删除角色: roleId={}", roleId);
    }

    @Override
    public Set<Long> getRolePermissionIds(Long roleId) {
        return rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolePermissions(Long roleId, List<Long> permissionIds) {
        // 删除原有权限关联
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, roleId));

        // 添加新的权限关联
        for (Long permissionId : permissionIds) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermissionMapper.insert(rolePermission);
        }

        log.info("分配角色权限: roleId={}, permissionIds={}", roleId, permissionIds);
    }

    @Override
    public List<Permission> getAllPermissions() {
        return permissionMapper.selectList(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getDeleted, 0)
                .orderByAsc(Permission::getSortOrder));
    }

    @Override
    public List<Permission> getPermissionTree() {
        List<Permission> allPermissions = getAllPermissions();
        return buildPermissionTree(allPermissions, 0L);
    }

    @Override
    public List<Department> getAllDepartments() {
        return departmentMapper.selectAll();
    }

    @Override
    public List<Department> getDepartmentTree() {
        List<Department> allDepts = getAllDepartments();
        return buildDepartmentTree(allDepts, 0L);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDepartment(DepartmentDTO deptDTO) {
        // 检查部门编码是否重复
        Department existing = departmentMapper.selectByDeptCode(deptDTO.getDeptCode());
        if (existing != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "部门编码已存在");
        }

        Department dept = new Department();
        BeanUtils.copyProperties(deptDTO, dept);
        dept.setStatus(1);
        departmentMapper.insert(dept);

        log.info("创建部门: {}", deptDTO.getDeptCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDepartment(Long deptId, DepartmentDTO deptDTO) {
        Department dept = departmentMapper.selectById(deptId);
        if (dept == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "部门不存在");
        }

        BeanUtils.copyProperties(deptDTO, dept);
        departmentMapper.updateById(dept);

        log.info("更新部门: deptId={}", deptId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartment(Long deptId) {
        Department dept = departmentMapper.selectById(deptId);
        if (dept == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "部门不存在");
        }

        // 检查是否有子部门
        List<Department> children = departmentMapper.selectByParentId(deptId);
        if (!children.isEmpty()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "该部门下有子部门，不能删除");
        }

        // 检查是否有用户属于该部门
        Long userCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getDeptId, deptId)
                .eq(User::getDeleted, 0));
        if (userCount > 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "该部门下有用户，不能删除");
        }

        departmentMapper.deleteById(deptId);

        log.info("删除部门: deptId={}", deptId);
    }

    // ========== 私有方法 ==========

    /**
     * 构建权限树
     */
    private List<Permission> buildPermissionTree(List<Permission> permissions, Long parentId) {
        return permissions.stream()
                .filter(p -> Objects.equals(p.getParentId(), parentId))
                .peek(p -> {
                    // 这里可以添加children字段来构建树结构
                    // 由于Permission实体没有children字段，需要使用Map或DTO
                })
                .collect(Collectors.toList());
    }

    /**
     * 构建部门树
     */
    private List<Department> buildDepartmentTree(List<Department> departments, Long parentId) {
        return departments.stream()
                .filter(d -> Objects.equals(d.getParentId(), parentId))
                .collect(Collectors.toList());
    }
}