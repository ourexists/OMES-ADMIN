package com.ourexists.omes.ai.agent.aiservice;

import com.ourexists.omes.ai.shared.service.MultiProviderLlmService;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.stereotype.Component;

@Component
public class AiAgentFactory {

    private final MultiProviderLlmService llmService;
    private final ChatMemoryProvider chatMemoryProvider;

    public AiAgentFactory(MultiProviderLlmService llmService, ChatMemoryProvider chatMemoryProvider) {
        this.llmService = llmService;
        this.chatMemoryProvider = chatMemoryProvider;
    }

    public <T> T create(Class<T> agentType, String provider, String modelName, Object... tools) {
        ChatModel chatModel = llmService.resolveChatModel(provider, modelName);
        AiServices<T> builder = AiServices.builder(agentType).chatModel(chatModel);
        if (tools != null && tools.length > 0) {
            builder.tools(tools);
        }
        return builder.build();
    }

    /**
     * 使用会话级持久化记忆（memoryId 由调用方传入，通常为 sessionId），与无记忆的 {@link #create} 并存。
     */
    public <T> T createWithSessionMemory(Class<T> agentType, String provider, String modelName, Object... tools) {
        ChatModel chatModel = llmService.resolveChatModel(provider, modelName);
        AiServices<T> builder = AiServices.builder(agentType)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider);
        if (tools != null && tools.length > 0) {
            builder.tools(tools);
        }
        return builder.build();
    }
}
