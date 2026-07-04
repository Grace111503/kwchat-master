package com.kwp.chat.model.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * AI模型配置DTO
 */
@Data
public class AiModelConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模型名称
     */
    @NotBlank(message = "模型名称不能为空")
    private String modelName;

    /**
     * 模型提供商
     */
    @NotBlank(message = "提供商不能为空")
    private String provider;

    /**
     * API地址
     */
    @NotBlank(message = "API地址不能为空")
    private String apiUrl;

    /**
     * API密钥
     */
    @NotBlank(message = "API密钥不能为空")
    private String apiKey;

    /**
     * 模型标识
     */
    @NotBlank(message = "模型标识不能为空")
    private String modelId;

    /**
     * 最大Token数
     */
    private Integer maxTokens = 4096;

    /**
     * 温度参数
     */
    private Double temperature = 0.7;

    /**
     * 是否启用
     */
    private Integer enabled = 1;

    /**
     * 是否默认模型
     */
    private Integer isDefault = 0;

    /**
     * 排序
     */
    private Integer sortOrder = 0;

    /**
     * 备注
     */
    private String remark;
}