package com.kwp.chat.service.impl;

import com.kwp.chat.model.system.AiModelConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;
// ... existing code ...


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class AiClientFactory {

    private final Map<String, ChatClient> clientCache = new ConcurrentHashMap<>();

    public ChatClient getChatClient(AiModelConfig config) {
        String cacheKey = config.getId() + "-" + config.getUpdateTime();
        return clientCache.computeIfAbsent(cacheKey, k -> createChatClient(config));
    }

    public ChatClient getDefaultChatClient() {
        return clientCache.values().stream().findFirst().orElse(null);
    }

    public void invalidateClient(Long configId) {
        clientCache.keySet().removeIf(key -> key.startsWith(configId.toString()));
        log.info("Invalidated AI client cache for config: {}", configId);
    }

    public void invalidateAllClients() {
        clientCache.clear();
        log.info("Invalidated all AI client caches");
    }

    private ChatClient createChatClient(AiModelConfig config) {
        try {
            OpenAiApi openAiApi = new OpenAiApi(config.getApiUrl(), config.getApiKey());
            
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .model(config.getModelId())
                    .temperature(config.getTemperature())
                    .maxTokens(config.getMaxTokens())
                    .build();
            
            ChatModel chatModel = new OpenAiChatModel(openAiApi, options);
            
            return ChatClient.create(chatModel);
        } catch (Exception e) {
            log.error("Failed to create AI client for model: {}", config.getModelName(), e);
            throw new RuntimeException("Failed to create AI client", e);
        }
    }

    public boolean testConnection(AiModelConfig config) {
        try {
            ChatClient client = createChatClient(config);
            String response = client.prompt("Hello")
                    .call()
                    .content();
            log.info("AI connection test successful for model: {}", config.getModelName());
            return response != null && !response.isEmpty();
        } catch (Exception e) {
            log.error("AI connection test failed for model: {}", config.getModelName(), e);
            return false;
        }
    }
}