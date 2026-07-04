package com.kwp.chat.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 翻译请求DTO
 */
@Data
public class TranslateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 待翻译文本
     */
    @NotBlank(message = "翻译内容不能为空")
    private String text;

    /**
     * 源语言（auto：自动检测）
     */
    private String sourceLanguage = "auto";

    /**
     * 目标语言
     */
    @NotBlank(message = "目标语言不能为空")
    private String targetLanguage = "en";
}