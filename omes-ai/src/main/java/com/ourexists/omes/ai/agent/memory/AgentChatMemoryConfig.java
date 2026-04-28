package com.ourexists.omes.ai.agent.memory;

import com.ourexists.omes.ai.config.AgentProperties;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 与 LangChain4j 示例一致：按 memoryId（此处为 sessionId）提供带持久化 Store 的窗口记忆。
 */
@Configuration
public class AgentChatMemoryConfig {

    @Bean
    public ChatMemoryProvider agentChatMemoryProvider(AgentSessionChatMemoryStore chatMemoryStore,
                                                      AgentProperties agentProperties) {
        return memoryId -> {
            int maxMessages = resolveWindowSize(agentProperties);
            return MessageWindowChatMemory.builder()
                    .id(memoryId)
                    .maxMessages(maxMessages)
                    .chatMemoryStore(chatMemoryStore)
                    .build();
        };
    }

    private static int resolveWindowSize(AgentProperties agentProperties) {
        int configured = agentProperties.getMultiAgent().getMemoryWindowMessages();
        if (configured > 0) {
            return configured;
        }
        return Math.max(1, agentProperties.getMultiAgent().getMaxHistory());
    }
}
