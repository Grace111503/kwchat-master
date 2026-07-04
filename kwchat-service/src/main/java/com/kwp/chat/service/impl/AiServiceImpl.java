package com.kwp.chat.service.impl;

import com.kwp.chat.common.exception.BusinessException;
import com.kwp.chat.common.result.ResultCode;
import com.kwp.chat.dao.MessageMapper;
import com.kwp.chat.model.dto.AiResponse;
import com.kwp.chat.model.message.Message;
import com.kwp.chat.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final MessageMapper messageMapper;

    @Value("${ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.model:gpt-3.5-turbo}")
    private String model;

    @Override
    public AiResponse generateSummary(Long conversationId, Integer messageLimit, String summaryType) {
        long startTime = System.currentTimeMillis();

        try {
            // 获取最近的聊天记录
            List<Message> messages = messageMapper.selectByConversationId(conversationId, messageLimit, 0);

            if (messages.isEmpty()) {
                return AiResponse.builder()
                        .content("暂无聊天记录可供总结")
                        .processingTime(System.currentTimeMillis() - startTime)
                        .build();
            }

            // 构建聊天记录文本
            String chatHistory = buildChatHistory(messages);

            // 生成摘要
            String summary;
            if (aiEnabled) {
                summary = callAiForSummary(chatHistory, summaryType);
            } else {
                summary = generateLocalSummary(messages, summaryType);
            }

            return AiResponse.builder()
                    .content(summary)
                    .tokensUsed(estimateTokens(chatHistory))
                    .processingTime(System.currentTimeMillis() - startTime)
                    .build();

        } catch (Exception e) {
            log.error("生成摘要失败: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.AI_REQUEST_FAILED, "生成摘要失败: " + e.getMessage());
        }
    }

    @Override
    public AiResponse translate(String text, String sourceLanguage, String targetLanguage) {
        long startTime = System.currentTimeMillis();

        try {
            String translatedText;
            if (aiEnabled) {
                translatedText = callAiForTranslation(text, sourceLanguage, targetLanguage);
            } else {
                translatedText = generateLocalTranslation(text, targetLanguage);
            }

            return AiResponse.builder()
                    .content(translatedText)
                    .originalContent(text)
                    .sourceLanguage(sourceLanguage)
                    .targetLanguage(targetLanguage)
                    .tokensUsed(estimateTokens(text))
                    .processingTime(System.currentTimeMillis() - startTime)
                    .build();

        } catch (Exception e) {
            log.error("翻译失败: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.AI_REQUEST_FAILED, "翻译失败: " + e.getMessage());
        }
    }

    @Override
    public AiResponse translateMessage(Long messageId, String targetLanguage) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(ResultCode.MESSAGE_NOT_FOUND);
        }

        return translate(message.getContent(), "auto", targetLanguage);
    }

    @Override
    public List<String> suggestReplies(Long conversationId) {
        try {
            // 获取最近的聊天记录
            List<Message> messages = messageMapper.selectByConversationId(conversationId, 10, 0);

            if (messages.isEmpty()) {
                return getDefaultSuggestions();
            }

            if (aiEnabled) {
                return callAiForSuggestions(messages);
            } else {
                return getDefaultSuggestions();
            }

        } catch (Exception e) {
            log.error("获取回复建议失败: {}", e.getMessage());
            return getDefaultSuggestions();
        }
    }

    /**
     * 构建聊天记录文本
     */
    private String buildChatHistory(List<Message> messages) {
        return messages.stream()
                .map(msg -> {
                    String sender = msg.getSenderId() != null ? "用户" + msg.getSenderId() : "系统";
                    String content = msg.getContent();
                    if (msg.getMessageType() != 1) {
                        content = getMessageTypeDesc(msg.getMessageType());
                    }
                    return sender + ": " + content;
                })
                .collect(Collectors.joining("\n"));
    }

    /**
     * 获取消息类型描述
     */
    private String getMessageTypeDesc(Integer messageType) {
        return switch (messageType) {
            case 2 -> "[图片]";
            case 3 -> "[文件]";
            case 4 -> "[视频]";
            case 5 -> "[语音]";
            case 6 -> "[系统消息]";
            case 7 -> "[消息已撤回]";
            default -> "[消息]";
        };
    }

    /**
     * 调用AI生成摘要（真实实现）
     */
    private String callAiForSummary(String chatHistory, String summaryType) {
        // TODO: 接入真实的AI API（如OpenAI、Claude等）
        // 这里是示例代码框架

        String prompt = switch (summaryType) {
            case "detailed" -> "请详细总结以下聊天记录，包括主要讨论的话题、达成的共识和待办事项：\n\n";
            case "key_points" -> "请提取以下聊天记录的关键要点，以列表形式呈现：\n\n";
            default -> "请简要总结以下聊天记录的主要内容：\n\n";
        };

        // 模拟AI响应
        return generateLocalSummary(null, summaryType);
    }

    /**
     * 调用AI进行翻译（真实实现）
     */
    private String callAiForTranslation(String text, String sourceLanguage, String targetLanguage) {
        // TODO: 接入真实的AI API
        // 这里是示例代码框架

        String prompt = String.format("请将以下文本翻译成%s：\n\n%s", getLanguageName(targetLanguage), text);

        // 模拟翻译响应
        return generateLocalTranslation(text, targetLanguage);
    }

    /**
     * 调用AI获取回复建议（真实实现）
     */
    private List<String> callAiForSuggestions(List<Message> messages) {
        // TODO: 接入真实的AI API
        return getDefaultSuggestions();
    }

    /**
     * 本地生成摘要（模拟）
     */
    private String generateLocalSummary(List<Message> messages, String summaryType) {
        if (messages == null || messages.isEmpty()) {
            return "暂无聊天记录";
        }

        int messageCount = messages.size();
        int textCount = (int) messages.stream().filter(m -> m.getMessageType() == 1).count();
        int imageCount = (int) messages.stream().filter(m -> m.getMessageType() == 2).count();
        int fileCount = (int) messages.stream().filter(m -> m.getMessageType() == 3).count();

        StringBuilder summary = new StringBuilder();

        switch (summaryType) {
            case "detailed":
                summary.append("【聊天记录详细总结】\n\n");
                summary.append(String.format("本次会话共有 %d 条消息，其中：\n", messageCount));
                summary.append(String.format("- 文本消息：%d 条\n", textCount));
                summary.append(String.format("- 图片消息：%d 条\n", imageCount));
                summary.append(String.format("- 文件消息：%d 条\n", fileCount));
                summary.append("\n主要讨论内容：\n");
                summary.append(extractKeywords(messages));
                break;

            case "key_points":
                summary.append("【关键要点】\n\n");
                summary.append("1. ").append(extractKeywords(messages));
                summary.append("\n2. 活跃度：").append(messageCount).append("条消息");
                summary.append("\n3. 内容类型：文本").append(textCount).append("条，图片").append(imageCount).append("条");
                break;

            default: // brief
                summary.append("会话共 ").append(messageCount).append(" 条消息，");
                summary.append("主要内容涉及：").append(extractKeywords(messages));
                break;
        }

        return summary.toString();
    }

    /**
     * 本地翻译（模拟）
     */
    private String generateLocalTranslation(String text, String targetLanguage) {
        // 这是一个简化的模拟翻译
        // 实际应用中应该调用翻译API

        String langName = getLanguageName(targetLanguage);

        // 简单的模拟翻译逻辑
        if ("en".equals(targetLanguage)) {
            return "[Translated to English] " + text;
        } else if ("zh".equals(targetLanguage)) {
            return "[翻译为中文] " + text;
        } else if ("ja".equals(targetLanguage)) {
            return "[日本語に翻訳] " + text;
        } else if ("ko".equals(targetLanguage)) {
            return "[한국어로 번역] " + text;
        }

        return "[Translated to " + langName + "] " + text;
    }

    /**
     * 提取关键词
     */
    private String extractKeywords(List<Message> messages) {
        // 简单的关键词提取
        List<String> keywords = new ArrayList<>();

        long textMessageCount = messages.stream()
                .filter(m -> m.getMessageType() == 1 && m.getContent() != null)
                .count();

        if (textMessageCount > 0) {
            keywords.add("文本交流");
        }

        boolean hasImage = messages.stream().anyMatch(m -> m.getMessageType() == 2);
        if (hasImage) {
            keywords.add("图片分享");
        }

        boolean hasFile = messages.stream().anyMatch(m -> m.getMessageType() == 3);
        if (hasFile) {
            keywords.add("文件传输");
        }

        if (keywords.isEmpty()) {
            return "日常交流";
        }

        return String.join("、", keywords);
    }

    /**
     * 获取语言名称
     */
    private String getLanguageName(String languageCode) {
        return switch (languageCode) {
            case "zh" -> "中文";
            case "en" -> "英语";
            case "ja" -> "日语";
            case "ko" -> "韩语";
            case "fr" -> "法语";
            case "de" -> "德语";
            case "es" -> "西班牙语";
            case "ru" -> "俄语";
            default -> languageCode;
        };
    }

    /**
     * 估算token数量
     */
    private Integer estimateTokens(String text) {
        if (text == null) return 0;
        // 简单估算：中文约1.5字符/token，英文约4字符/token
        return (int) (text.length() / 2.0);
    }

    /**
     * 获取默认回复建议
     */
    private List<String> getDefaultSuggestions() {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("好的");
        suggestions.add("收到，谢谢");
        suggestions.add("了解了");
        suggestions.add("没问题");
        suggestions.add("稍后回复");
        return suggestions;
    }
}