package com.kwp.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kwp.chat.model.auth.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

/**
 * 用户角色关联Mapper
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 查询用户的角色ID集合
     */
    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId} AND deleted = 0")
    Set<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 查询角色的用户ID集合
     */
    @Select("SELECT user_id FROM sys_user_role WHERE role_id = #{roleId} AND deleted = 0")
    Set<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 检查用户是否有指定角色
     */
    @Select("SELECT COUNT(*) FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId} AND deleted = 0")
    int countByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);
}