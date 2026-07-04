package com.kwp.chat.model.system;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kwp.chat.model.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI模型配置实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_ai_model_config")
public class AiModelConfig extends BaseEntity {

    /**
     * 模型名称
     */
    @TableField("model_name")
    private String modelName;

    /**
     * 模型提供商（openai, anthropic, baidu, alibaba等）
     */
    @TableField("provider")
    private String provider;

    /**
     * API地址
     */
    @TableField("api_url")
    private String apiUrl;

    /**
     * API密钥
     */
    @TableField("api_key")
    private String apiKey;

    /**
     * 模型标识
     */
    @TableField("model_id")
    private String modelId;

    /**
     * 最大Token数
     */
    @TableField("max_tokens")
    private Integer maxTokens;

    /**
     * 温度参数
     */
    @TableField("temperature")
    private Double temperature;

    /**
     * 是否启用（0：禁用，1：启用）
     */
    @TableField("enabled")
    private Integer enabled;

    /**
     * 是否默认模型（0：否，1：是）
     */
    @TableField("is_default")
    private Integer isDefault;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}