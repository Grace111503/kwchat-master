package com.kwp.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kwp.chat.model.system.LoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 登录日志Mapper
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {

    /**
     * 查询最近的登录日志
     */
    @Select("SELECT * FROM sys_login_log WHERE deleted = 0 ORDER BY login_time DESC LIMIT #{limit}")
    List<LoginLog> selectRecentLogs(@Param("limit") int limit);

    /**
     * 统计今日登录次数
     */
    @Select("SELECT COUNT(*) FROM sys_login_log WHERE DATE(login_time) = CURDATE() AND deleted = 0")
    Long countTodayLogins();

    /**
     * 统计今日登录成功次数
     */
    @Select("SELECT COUNT(*) FROM sys_login_log WHERE DATE(login_time) = CURDATE() AND status = 1 AND deleted = 0")
    Long countTodaySuccessLogins();
}