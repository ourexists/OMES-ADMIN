package com.ourexists.omes.ai.agent.memory;

import com.ourexists.omes.ai.agent.chat.store.AgentSessionStore;
import com.ourexists.omes.ai.agent.model.AgentMessageDto;
import com.ourexists.omes.ai.config.AgentProperties;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 长期记忆：以 {@link AgentSessionStore} 中的会话消息（MySQL）为权威来源，在缓存未命中时回放。
 * 短期记忆：LangChain4j 的 {@link dev.langchain4j.memory.chat.MessageWindowChatMemory} 与 Redis（若可用）
 * 或进程内 Map 中序列化的消息窗口，TTL 与 agent.session.short-term-ttl-minutes 一致。
 *
 * @see <a href="https://github.com/langchain4j/langchain4j-examples/blob/main/other-examples/src/main/java/ServiceWithPersistentMemoryForEachUserExample.java">ServiceWithPersistentMemoryForEachUserExample</a>
 */
@Component
public class AgentSessionChatMemoryStore implements ChatMemoryStore {

    private static final String REDIS_LC_MEMORY_KEY_PREFIX = ":agent:session:lc-memory:";

    private final AgentSessionStore sessionStore;
    private final StringRedisTemplate redisTemplate;
    private final AgentProperties agentProperties;
    private final ConcurrentHashMap<String, String> processLocalJson = new ConcurrentHashMap<>();

    public AgentSessionChatMemoryStore(AgentSessionStore sessionStore,
                                       ObjectProvider<StringRedisTemplate> redisTemplateProvider,
                                       AgentProperties agentProperties) {
        this.sessionStore = sessionStore;
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
        this.agentProperties = agentProperties;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String sessionId = String.valueOf(memoryId);
        if (sessionId.isBlank()) {
            return List.of();
        }
        String json = readJson(sessionId);
        if (json != null && !json.isBlank()) {
            return ChatMessageDeserializer.messagesFromJson(json);
        }
        List<ChatMessage> seeded = seedFromDatabase(sessionId);
        if (!seeded.isEmpty()) {
            String serialized = ChatMessageSerializer.messagesToJson(seeded);
            writeJson(sessionId, serialized);
            return seeded;
        }
        return List.of();
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String sessionId = String.valueOf(memoryId);
        if (sessionId.isBlank()) {
            return;
        }
        String json = ChatMessageSerializer.messagesToJson(messages == null ? List.of() : messages);
        writeJson(sessionId, json);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String sessionId = String.valueOf(memoryId);
        if (sessionId.isBlank()) {
            return;
        }
        processLocalJson.remove(sessionId);
        if (redisTemplate != null) {
            redisTemplate.delete(redisKey(sessionId));
        }
    }

    private List<ChatMessage> seedFromDatabase(String sessionId) {
        List<AgentMessageDto> rows = sessionStore.listMessages(sessionId);
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        int seedCap = resolveSeedCap();
        int start = Math.max(0, rows.size() - seedCap);
        List<ChatMessage> out = new ArrayList<>(Math.min(seedCap, rows.size()));
        for (int i = start; i < rows.size(); i++) {
            AgentMessageDto dto = rows.get(i);
            if (dto == null || dto.getContent() == null || dto.getContent().isBlank()) {
                continue;
            }
            out.add(toChatMessage(dto));
        }
        return out;
    }

    private static ChatMessage toChatMessage(AgentMessageDto dto) {
        String role = dto.getRole() == null ? "" : dto.getRole().trim();
        String text = dto.getContent().trim();
        if ("user".equalsIgnoreCase(role)) {
            return UserMessage.from(text);
        }
        return AiMessage.from(text);
    }

    private int resolveSeedCap() {
        int maxHistory = Math.max(1, agentProperties.getMultiAgent().getMaxHistory());
        return Math.max(100, maxHistory * 5);
    }

    private long redisTtlMinutes() {
        int minutes = agentProperties.getSession().getShortTermTtlMinutes();
        return Math.max(1L, minutes);
    }

    private String readJson(String sessionId) {
        if (redisTemplate != null) {
            String v = redisTemplate.opsForValue().get(redisKey(sessionId));
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return processLocalJson.get(sessionId);
    }

    private void writeJson(String sessionId, String json) {
        processLocalJson.put(sessionId, json);
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(redisKey(sessionId), json, redisTtlMinutes(), TimeUnit.MINUTES);
        }
    }

    private String redisKey(String sessionId) {
        return REDIS_LC_MEMORY_KEY_PREFIX + sessionId;
    }
}
