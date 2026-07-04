package com.kwp.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kwp.chat.model.system.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统配置Mapper
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    /**
     * 根据配置键查询
     */
    @Select("SELECT * FROM sys_config WHERE config_key = #{configKey} AND deleted = 0")
    SystemConfig selectByConfigKey(@Param("configKey") String configKey);

    /**
     * 根据分组查询
     */
    @Select("SELECT * FROM sys_config WHERE config_group = #{configGroup} AND deleted = 0 ORDER BY create_time")
    List<SystemConfig> selectByGroup(@Param("configGroup") String configGroup);

    /**
     * 查询所有配置
     */
    @Select("SELECT * FROM sys_config WHERE deleted = 0 ORDER BY config_group, create_time")
    List<SystemConfig> selectAll();
}