package com.kwp.chat.service.impl;

import com.kwp.chat.common.exception.BusinessException;
import com.kwp.chat.common.result.ResultCode;
import com.kwp.chat.dao.AiModelConfigMapper;
import com.kwp.chat.dao.MessageMapper;
import com.kwp.chat.model.dto.AiResponse;
import com.kwp.chat.model.message.Message;
import com.kwp.chat.model.system.AiModelConfig;
import com.kwp.chat.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final MessageMapper messageMapper;
    private final AiModelConfigMapper aiModelConfigMapper;
    private final AiClientFactory aiClientFactory;

    @Override
    public AiResponse generateSummary(Long conversationId, Integer messageLimit, String summaryType) {
        long startTime = System.currentTimeMillis();

        try {
            List<Message> messages = messageMapper.selectByConversationId(conversationId, messageLimit, 0);

            if (messages.isEmpty()) {
                return AiResponse.builder()
                        .content("暂无聊天记录可供总结")
                        .processingTime(System.currentTimeMillis() - startTime)
                        .build();
            }

            String chatHistory = buildChatHistory(messages);

            String summary;
            AiModelConfig config = getDefaultAiConfig();
            if (config != null) {
                summary = callAiForSummary(config, chatHistory, summaryType);
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
            AiModelConfig config = getDefaultAiConfig();
            if (config != null) {
                translatedText = callAiForTranslation(config, text, sourceLanguage, targetLanguage);
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
            List<Message> messages = messageMapper.selectByConversationId(conversationId, 10, 0);

            if (messages.isEmpty()) {
                return getDefaultSuggestions();
            }

            AiModelConfig config = getDefaultAiConfig();
            if (config != null) {
                return callAiForSuggestions(config, messages);
            } else {
                return getDefaultSuggestions();
            }

        } catch (Exception e) {
            log.error("获取回复建议失败: {}", e.getMessage());
            return getDefaultSuggestions();
        }
    }

    private AiModelConfig getDefaultAiConfig() {
        AiModelConfig config = aiModelConfigMapper.selectDefaultModel();
        if (config == null) {
            List<AiModelConfig> enabledModels = aiModelConfigMapper.selectEnabledModels();
            if (!enabledModels.isEmpty()) {
                config = enabledModels.get(0);
            }
        }
        return config;
    }

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

    private String getMessageTypeDesc(Integer messageType) {
        if (messageType == 2) {
            return "[图片]";
        } else if (messageType == 3) {
            return "[文件]";
        } else if (messageType == 4) {
            return "[视频]";
        } else if (messageType == 5) {
            return "[语音]";
        } else if (messageType == 6) {
            return "[系统消息]";
        } else if (messageType == 7) {
            return "[消息已撤回]";
        }
        return "[消息]";
    }

    private String callAiForSummary(AiModelConfig config, String chatHistory, String summaryType) {
        ChatClient client = aiClientFactory.getChatClient(config);

        String prompt;
        if ("detailed".equals(summaryType)) {
            prompt = "请详细总结以下聊天记录，包括主要讨论的话题、达成的共识和待办事项：\n\n";
        } else if ("key_points".equals(summaryType)) {
            prompt = "请提取以下聊天记录的关键要点，以列表形式呈现：\n\n";
        } else {
            prompt = "请简要总结以下聊天记录的主要内容：\n\n";
        }

        String response = client.prompt(prompt + chatHistory)
                .call()
                .content();

        log.info("AI summary generated successfully, model: {}, type: {}", config.getModelName(), summaryType);
        return response;
    }

    private String callAiForTranslation(AiModelConfig config, String text, String sourceLanguage, String targetLanguage) {
        ChatClient client = aiClientFactory.getChatClient(config);

        String langName = getLanguageName(targetLanguage);
        String prompt;

        if ("auto".equals(sourceLanguage)) {
            prompt = String.format("请将以下文本翻译成%s：\n\n%s", langName, text);
        } else {
            String sourceLangName = getLanguageName(sourceLanguage);
            prompt = String.format("请将以下%s文本翻译成%s：\n\n%s", sourceLangName, langName, text);
        }

        String response = client.prompt(prompt)
                .call()
                .content();

        log.info("AI translation completed, model: {}, targetLang: {}", config.getModelName(), targetLanguage);
        return response;
    }

    private List<String> callAiForSuggestions(AiModelConfig config, List<Message> messages) {
        ChatClient client = aiClientFactory.getChatClient(config);

        String chatHistory = buildChatHistory(messages);
        String prompt = "请根据以下聊天记录，提供3-5个合适的回复建议。要求：\n" +
                "1. 回复要自然、得体\n" +
                "2. 符合上下文语境\n" +
                "3. 每个建议单独一行，不要编号\n" +
                "\n" +
                "聊天记录：\n" +
                chatHistory;

        String response = client.prompt(prompt)
                .call()
                .content();

        if (response == null || response.isEmpty()) {
            return getDefaultSuggestions();
        }

        return response.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .limit(5)
                .collect(Collectors.toList());
    }

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

            default:
                summary.append("会话共 ").append(messageCount).append(" 条消息，");
                summary.append("主要内容涉及：").append(extractKeywords(messages));
                break;
        }

        return summary.toString();
    }

    private String generateLocalTranslation(String text, String targetLanguage) {
        String langName = getLanguageName(targetLanguage);

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

    private String extractKeywords(List<Message> messages) {
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

    private String getLanguageName(String languageCode) {
        if ("zh".equals(languageCode)) {
            return "中文";
        } else if ("en".equals(languageCode)) {
            return "英语";
        } else if ("ja".equals(languageCode)) {
            return "日语";
        } else if ("ko".equals(languageCode)) {
            return "韩语";
        } else if ("fr".equals(languageCode)) {
            return "法语";
        } else if ("de".equals(languageCode)) {
            return "德语";
        } else if ("es".equals(languageCode)) {
            return "西班牙语";
        } else if ("ru".equals(languageCode)) {
            return "俄语";
        } else if ("ar".equals(languageCode)) {
            return "阿拉伯语";
        } else if ("pt".equals(languageCode)) {
            return "葡萄牙语";
        }
        return languageCode;
    }

    private Integer estimateTokens(String text) {
        if (text == null) return 0;
        return (int) (text.length() / 2.0);
    }

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