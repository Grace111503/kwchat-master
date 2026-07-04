package com.kwp.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kwp.chat.model.system.AiModelConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI模型配置Mapper
 */
@Mapper
public interface AiModelConfigMapper extends BaseMapper<AiModelConfig> {

    /**
     * 查询所有启用的模型
     */
    @Select("SELECT * FROM sys_ai_model_config WHERE enabled = 1 AND deleted = 0 ORDER BY sort_order")
    List<AiModelConfig> selectEnabledModels();

    /**
     * 查询默认模型
     */
    @Select("SELECT * FROM sys_ai_model_config WHERE is_default = 1 AND enabled = 1 AND deleted = 0 LIMIT 1")
    AiModelConfig selectDefaultModel();

    /**
     * 根据提供商查询
     */
    @Select("SELECT * FROM sys_ai_model_config WHERE provider = #{provider} AND deleted = 0 ORDER BY sort_order")
    List<AiModelConfig> selectByProvider(@Param("provider") String provider);
}