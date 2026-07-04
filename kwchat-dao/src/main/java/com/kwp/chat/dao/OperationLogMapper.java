package com.kwp.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kwp.chat.model.system.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 操作日志Mapper
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    /**
     * 查询最近的日志
     */
    @Select("SELECT * FROM sys_operation_log WHERE deleted = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<OperationLog> selectRecentLogs(@Param("limit") int limit);

    /**
     * 统计今日操作数
     */
    @Select("SELECT COUNT(*) FROM sys_operation_log WHERE DATE(create_time) = CURDATE() AND deleted = 0")
    Long countTodayOperations();

    /**
     * 按模块统计操作数
     */
    @Select("SELECT module, COUNT(*) as count FROM sys_operation_log WHERE deleted = 0 GROUP BY module ORDER BY count DESC LIMIT #{limit}")
    List<Map<String, Object>> countByModule(@Param("limit") int limit);
}