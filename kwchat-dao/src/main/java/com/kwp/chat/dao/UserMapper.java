package com.kwp.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kwp.chat.model.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM sys_user WHERE phone = #{phone} AND deleted = 0")
    User selectByPhone(@Param("phone") String phone);

    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM sys_user WHERE email = #{email} AND deleted = 0")
    User selectByEmail(@Param("email") String email);

    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE username = #{username} AND deleted = 0")
    int countByUsername(@Param("username") String username);

    /**
     * 检查手机号是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE phone = #{phone} AND deleted = 0")
    int countByPhone(@Param("phone") String phone);

    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE email = #{email} AND deleted = 0")
    int countByEmail(@Param("email") String email);

    /**
     * 根据ID列表批量查询用户
     */
    @Select("<script>" +
            "SELECT * FROM sys_user WHERE deleted = 0 AND id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<User> selectByIds(@Param("ids") List<Long> ids);
}