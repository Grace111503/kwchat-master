package com.kwp.chat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * AI响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应内容
     */
    private String content;

    /**
     * 原始内容（翻译时为原文）
     */
    private String originalContent;

    /**
     * 目标语言（翻译时使用）
     */
    private String targetLanguage;

    /**
     * 源语言（翻译时使用）
     */
    private String sourceLanguage;

    /**
     * 使用的token数量
     */
    private Integer tokensUsed;

    /**
     * 处理时间（毫秒）
     */
    private Long processingTime;
}