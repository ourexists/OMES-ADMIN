package com.ourexists.omes.ai.shared.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiLlmService {

    private final ChatClient chatClient;

    public AiLlmService(ObjectProvider<ChatClient.Builder> chatClientBuilderProvider) {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        this.chatClient = builder != null ? builder.build() : null;
    }

    public String ask(String prompt) {
        if (chatClient == null) {
            return "【本地降级报告】\n当前未配置可用LLM，已输出规则摘要。\n\n" + prompt;
        }
        try {
            return chatClient.prompt().user(prompt).call().content();
        } catch (Exception ex) {
            log.warn("Call LLM failed, fallback to rule summary: {}", ex.getMessage());
            return "【LLM调用失败，返回规则摘要】\n" + prompt;
        }
    }

    public String modelSummary() {
        return chatClient != null ? "spring-ai-chat-client" : "rule-based-fallback";
    }
}
