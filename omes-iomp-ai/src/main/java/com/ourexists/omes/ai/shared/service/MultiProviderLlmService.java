package com.ourexists.omes.ai.shared.service;

import com.ourexists.omes.ai.config.AiLlmProviderProperties;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class MultiProviderLlmService {

    private final ChatModel defaultChatModel;
    private final String defaultProvider;
    private final String defaultModelName;
    private final AiLlmProviderProperties providerProperties;
    private final Map<String, ChatModel> chatModelCache = new ConcurrentHashMap<>();

    public MultiProviderLlmService(AiLlmProviderProperties providerProperties) {
        this.providerProperties = providerProperties;
        this.defaultProvider = normalizeProvider(providerProperties.getDefaultProvider());
        AiLlmProviderProperties.ProviderConfig provider = resolveProviderConfig(this.defaultProvider);
        this.defaultModelName = fallbackString(provider.getDefaultModel(), "gpt-4o-mini");
        ChatModel initializedModel = createModel(this.defaultProvider, this.defaultModelName);
        this.defaultChatModel = initializedModel;
        if (initializedModel != null) {
            chatModelCache.put(cacheKey(this.defaultProvider, this.defaultModelName), initializedModel);
        }
    }

    public String ask(String prompt) {
        return askWithModel(defaultProvider, defaultModelName, prompt);
    }

    public String askWithModel(String provider, String modelName, String prompt) {
        ChatModel chatModel = resolveModel(provider, modelName);
        if (chatModel == null) {
            return "【本地降级报告】\n当前未配置可用LLM，已输出规则摘要。\n\n" + prompt;
        }
        try {
            return chatModel.chat(prompt);
        } catch (Exception ex) {
            log.warn("Call LLM failed, fallback to rule summary: {}", ex.getMessage());
            return "【LLM调用失败，返回规则摘要】\n" + prompt;
        }
    }

    public String askWithToolsAndModel(String provider, String modelName, String prompt, Object... tools) {
        ChatModel chatModel = resolveModel(provider, modelName);
        if (chatModel == null) {
            return "【本地降级报告】\n当前未配置可用LLM，已输出规则摘要。\n\n" + prompt;
        }
        try {
            if (tools == null || tools.length == 0) {
                return chatModel.chat(prompt);
            }
            Assistant assistant = AiServices.builder(Assistant.class)
                    .chatModel(chatModel)
                    .tools(tools)
                    .build();
            return assistant.chat(prompt);
        } catch (Exception ex) {
            log.warn("Call LLM with tools failed, fallback to rule summary: {}", ex.getMessage());
            return "【LLM调用失败，返回规则摘要】\n" + prompt;
        }
    }

    public ChatModel resolveChatModel(String provider, String modelName) {
        ChatModel chatModel = resolveModel(provider, modelName);
        if (chatModel == null) {
            throw new IllegalStateException("No available chat model for provider=" + provider + ", model=" + modelName);
        }
        return chatModel;
    }

    private ChatModel resolveModel(String provider, String modelName) {
        String actualProvider = normalizeProvider(provider);
        AiLlmProviderProperties.ProviderConfig providerConfig = resolveProviderConfig(actualProvider);
        if (providerConfig == null || providerConfig.getApiKey() == null || providerConfig.getApiKey().isBlank()) {
            log.warn("LLM provider '{}' not configured with apiKey, fallback default", actualProvider);
            if (actualProvider.equals(defaultProvider)) {
                return defaultChatModel;
            }
            return resolveModel(defaultProvider, modelName);
        }
        String actualModel = fallbackString(modelName, providerConfig.getDefaultModel());
        if (actualModel == null || actualModel.isBlank()) {
            return defaultChatModel;
        }
        String cacheKey = cacheKey(actualProvider, actualModel);
        ChatModel cached = chatModelCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        ChatModel created = createModel(actualProvider, actualModel);
        if (created != null) {
            chatModelCache.put(cacheKey, created);
            return created;
        }
        if (!actualProvider.equals(defaultProvider)) {
            log.warn("Init model '{}:{}' failed, fallback default", actualProvider, actualModel);
            return defaultChatModel;
        }
        return null;
    }

    private ChatModel createModel(String providerName, String modelName) {
        AiLlmProviderProperties.ProviderConfig provider = resolveProviderConfig(providerName);
        if (provider == null || provider.getApiKey() == null || provider.getApiKey().isBlank()) {
            return null;
        }
        String baseUrl = normalizeBaseUrl(provider.getBaseUrl());
        try {
            return OpenAiChatModel.builder()
                    .apiKey(provider.getApiKey().trim())
                    .baseUrl(baseUrl)
                    .modelName(modelName)
                    .build();
        } catch (Exception ex) {
            log.warn("Init model '{}:{}' failed: {}", providerName, modelName, ex.getMessage());
            return null;
        }
    }

    private AiLlmProviderProperties.ProviderConfig resolveProviderConfig(String providerName) {
        String p = normalizeProvider(providerName);
        if ("deepseek".equals(p)) {
            return providerProperties.getDeepseek();
        }
        if ("qwen".equals(p)) {
            return providerProperties.getQwen();
        }
        if ("anthropic".equals(p)) {
            return providerProperties.getAnthropic();
        }
        if ("gemini".equals(p)) {
            return providerProperties.getGemini();
        }
        return providerProperties.getOpenai();
    }

    private String normalizeProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            return "openai";
        }
        return provider.trim().toLowerCase();
    }

    private String fallbackString(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        if (second != null && !second.isBlank()) {
            return second.trim();
        }
        return "gpt-4o-mini";
    }

    private String cacheKey(String provider, String model) {
        return normalizeProvider(provider) + ":" + model;
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "https://api.openai.com";
        }
        String value = baseUrl.trim();
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    interface Assistant {
        String chat(String userMessage);
    }
}
