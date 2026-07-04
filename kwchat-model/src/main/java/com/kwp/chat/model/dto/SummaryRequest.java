package com.kwp.chat.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 智能摘要请求DTO
 */
@Data
public class SummaryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    @NotNull(message = "会话ID不能为空")
    private Long conversationId;

    /**
     * 消息数量限制（最近N条消息）
     */
    private Integer messageLimit = 100;

    /**
     * 摘要类型：brief（简要）、detailed（详细）、key_points（要点）
     */
    private String summaryType = "brief";
}