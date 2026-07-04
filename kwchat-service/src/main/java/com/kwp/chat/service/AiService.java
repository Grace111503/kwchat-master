package com.kwp.chat.service;

import com.kwp.chat.model.dto.AiResponse;

/**
 * AI服务接口
 */
public interface AiService {

    /**
     * 生成聊天记录摘要
     *
     * @param conversationId 会话ID
     * @param messageLimit   消息数量限制
     * @param summaryType    摘要类型：brief（简要）、detailed（详细）、key_points（要点）
     * @return 摘要内容
     */
    AiResponse generateSummary(Long conversationId, Integer messageLimit, String summaryType);

    /**
     * 翻译文本
     *
     * @param text           待翻译文本
     * @param sourceLanguage 源语言（auto：自动检测）
     * @param targetLanguage 目标语言
     * @return 翻译结果
     */
    AiResponse translate(String text, String sourceLanguage, String targetLanguage);

    /**
     * 翻译消息
     *
     * @param messageId      消息ID
     * @param targetLanguage 目标语言
     * @return 翻译结果
     */
    AiResponse translateMessage(Long messageId, String targetLanguage);

    /**
     * 智能回复建议
     *
     * @param conversationId 会话ID
     * @return 回复建议列表
     */
    java.util.List<String> suggestReplies(Long conversationId);
}