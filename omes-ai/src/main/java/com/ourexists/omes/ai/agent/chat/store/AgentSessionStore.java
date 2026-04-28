package com.ourexists.omes.ai.agent.chat.store;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.omes.ai.agent.chat.entity.AgentAuditLogEntity;
import com.ourexists.omes.ai.agent.chat.entity.AgentChatMessageEntity;
import com.ourexists.omes.ai.agent.chat.entity.AgentChatSessionEntity;
import com.ourexists.omes.ai.agent.chat.mapper.AgentAuditLogMapper;
import com.ourexists.omes.ai.agent.chat.mapper.AgentChatMessageMapper;
import com.ourexists.omes.ai.agent.chat.mapper.AgentChatSessionMapper;
import com.ourexists.omes.ai.agent.model.AgentMessageDto;
import com.ourexists.omes.ai.agent.model.AgentSessionDto;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class AgentSessionStore {

    private static final String REDIS_SESSION_MESSAGES_KEY_PREFIX = ":agent:session:messages:";
    private static final String REDIS_SESSIONS_KEY = ":agent:sessions";

    private final AgentChatSessionMapper sessionMapper;
    private final AgentChatMessageMapper messageMapper;
    private final AgentAuditLogMapper auditLogMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final long redisTtlMinutes;

    public AgentSessionStore(AgentChatSessionMapper sessionMapper,
                             AgentChatMessageMapper messageMapper,
                             AgentAuditLogMapper auditLogMapper,
                             ObjectProvider<StringRedisTemplate> redisTemplateProvider,
                             ObjectMapper objectMapper,
                             @Value("${agent.session.short-term-ttl-minutes:120}") long redisTtlMinutes) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.auditLogMapper = auditLogMapper;
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
        this.objectMapper = objectMapper;
        this.redisTtlMinutes = redisTtlMinutes;
    }

    public String createSession(String title, String operatorId) {
        String sessionId = UUID.randomUUID().toString();
        Date now = new Date();
        AgentChatSessionEntity entity = new AgentChatSessionEntity();
        entity.setSessionId(sessionId);
        entity.setTitle(title);
        entity.setOperatorId(operatorId);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        sessionMapper.insert(entity);
        refreshSessionsCache();
        return sessionId;
    }

    public void appendMessage(String sessionId, String role, String content) {
        Date now = new Date();
        String messageId = UUID.randomUUID().toString();
        AgentChatMessageEntity messageEntity = new AgentChatMessageEntity();
        messageEntity.setId(messageId);
        messageEntity.setSessionId(sessionId);
        messageEntity.setRole(role);
        messageEntity.setContent(content);
        messageEntity.setCreatedAt(now);
        messageMapper.insert(messageEntity);
        LambdaUpdateWrapper<AgentChatSessionEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AgentChatSessionEntity::getSessionId, sessionId)
                .set(AgentChatSessionEntity::getUpdatedAt, now);
        sessionMapper.update(null, updateWrapper);
        cacheMessage(sessionId, buildMessage(messageId, sessionId, role, content, now));
        refreshSessionsCache();
    }

    public List<AgentSessionDto> listSessions() {
        List<AgentSessionDto> cached = readCachedSessions();
        if (!cached.isEmpty()) {
            return cached;
        }
        List<AgentSessionDto> dbSessions = querySessionsFromDb();
        refreshSessionsCache(dbSessions);
        return dbSessions;
    }

    public List<AgentMessageDto> listMessages(String sessionId) {
        List<AgentMessageDto> cached = readCachedMessages(sessionId);
        if (!cached.isEmpty()) {
            return cached;
        }
        LambdaQueryWrapper<AgentChatMessageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentChatMessageEntity::getSessionId, sessionId)
                .orderByAsc(AgentChatMessageEntity::getCreatedAt);
        List<AgentMessageDto> dbMessages = toMessageDtos(messageMapper.selectList(wrapper));
        refreshMessageCache(sessionId, dbMessages);
        return dbMessages;
    }

    public void saveAudit(String sessionId, String operatorId, String action, boolean success, String message) {
        AgentAuditLogEntity entity = new AgentAuditLogEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setSessionId(sessionId);
        entity.setOperatorId(operatorId);
        entity.setAction(action);
        entity.setSuccessFlag(success ? 1 : 0);
        entity.setMessage(message);
        entity.setCreatedAt(new Date());
        auditLogMapper.insert(entity);
    }

    public AgentSessionDto getSession(String sessionId) {
        AgentChatSessionEntity entity = sessionMapper.selectById(sessionId);
        return entity == null ? null : toSessionDto(entity);
    }

    public boolean updateSessionTitle(String sessionId, String title) {
        LambdaUpdateWrapper<AgentChatSessionEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AgentChatSessionEntity::getSessionId, sessionId)
                .set(AgentChatSessionEntity::getTitle, title)
                .set(AgentChatSessionEntity::getUpdatedAt, new Date());
        int rows = sessionMapper.update(null, wrapper);
        if (rows > 0) {
            refreshSessionsCache();
        }
        return rows > 0;
    }

    public boolean deleteSession(String sessionId) {
        LambdaQueryWrapper<AgentChatMessageEntity> messageQuery = new LambdaQueryWrapper<>();
        messageQuery.eq(AgentChatMessageEntity::getSessionId, sessionId);
        messageMapper.delete(messageQuery);
        int rows = sessionMapper.deleteById(sessionId);
        clearMessageCache(sessionId);
        refreshSessionsCache();
        return rows > 0;
    }

    private AgentSessionDto toSessionDto(AgentChatSessionEntity entity) {
        AgentSessionDto dto = new AgentSessionDto();
        dto.setSessionId(entity.getSessionId());
        dto.setTitle(entity.getTitle());
        dto.setOperatorId(entity.getOperatorId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private AgentMessageDto toMessageDto(AgentChatMessageEntity entity) {
        AgentMessageDto dto = new AgentMessageDto();
        dto.setId(entity.getId());
        dto.setSessionId(entity.getSessionId());
        dto.setRole(entity.getRole());
        dto.setContent(entity.getContent());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private AgentMessageDto buildMessage(String id, String sessionId, String role, String content, Date createdAt) {
        AgentMessageDto dto = new AgentMessageDto();
        dto.setId(id);
        dto.setSessionId(sessionId);
        dto.setRole(role);
        dto.setContent(content);
        dto.setCreatedAt(createdAt);
        return dto;
    }

    private void cacheMessage(String sessionId, AgentMessageDto dto) {
        if (redisTemplate == null) {
            return;
        }
        String key = redisKey(sessionId);
        try {
            redisTemplate.opsForList().rightPush(key, objectMapper.writeValueAsString(dto));
            redisTemplate.expire(key, redisTtlMinutes, TimeUnit.MINUTES);
        } catch (JsonProcessingException ignored) {
            // Skip cache write when serialization fls; DB remns source of truth.
        }
    }

    private List<AgentMessageDto> readCachedMessages(String sessionId) {
        if (redisTemplate == null) {
            return List.of();
        }
        String key = redisKey(sessionId);
        Long size = redisTemplate.opsForList().size(key);
        if (size == null || size <= 0) {
            return List.of();
        }
        List<String> raw = redisTemplate.opsForList().range(key, 0, -1);
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        List<AgentMessageDto> messages = new ArrayList<>();
        for (String item : raw) {
            if (item == null || item.isBlank()) {
                continue;
            }
            try {
                messages.add(objectMapper.readValue(item, AgentMessageDto.class));
            } catch (JsonProcessingException ignored) {
                // Ignore malformed cache entries.
            }
        }
        return messages;
    }

    private void refreshMessageCache(String sessionId, List<AgentMessageDto> messages) {
        if (redisTemplate == null) {
            return;
        }
        String key = redisKey(sessionId);
        redisTemplate.delete(key);
        if (messages == null || messages.isEmpty()) {
            return;
        }
        for (AgentMessageDto message : messages) {
            cacheMessage(sessionId, message);
        }
    }

    private String redisKey(String sessionId) {
        return REDIS_SESSION_MESSAGES_KEY_PREFIX + sessionId;
    }

    private List<AgentSessionDto> querySessionsFromDb() {
        LambdaQueryWrapper<AgentChatSessionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AgentChatSessionEntity::getUpdatedAt);
        return toSessionDtos(sessionMapper.selectList(wrapper));
    }

    private List<AgentSessionDto> readCachedSessions() {
        if (redisTemplate == null) {
            return List.of();
        }
        Long size = redisTemplate.opsForList().size(REDIS_SESSIONS_KEY);
        if (size == null || size <= 0) {
            return List.of();
        }
        List<String> raw = redisTemplate.opsForList().range(REDIS_SESSIONS_KEY, 0, -1);
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        List<AgentSessionDto> sessions = new ArrayList<>();
        for (String item : raw) {
            if (item == null || item.isBlank()) {
                continue;
            }
            try {
                sessions.add(objectMapper.readValue(item, AgentSessionDto.class));
            } catch (JsonProcessingException ignored) {
                // Ignore malformed cache entries.
            }
        }
        return sessions;
    }

    private void refreshSessionsCache() {
        List<AgentSessionDto> sessions = querySessionsFromDb();
        refreshSessionsCache(sessions);
    }

    private void refreshSessionsCache(List<AgentSessionDto> sessions) {
        if (redisTemplate == null) {
            return;
        }
        redisTemplate.delete(REDIS_SESSIONS_KEY);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        for (AgentSessionDto session : sessions) {
            try {
                redisTemplate.opsForList().rightPush(REDIS_SESSIONS_KEY, objectMapper.writeValueAsString(session));
            } catch (JsonProcessingException ignored) {
                // Skip malformed entry.
            }
        }
        redisTemplate.expire(REDIS_SESSIONS_KEY, redisTtlMinutes, TimeUnit.MINUTES);
    }

    private void clearMessageCache(String sessionId) {
        if (redisTemplate == null) {
            return;
        }
        redisTemplate.delete(redisKey(sessionId));
    }

    private List<AgentSessionDto> toSessionDtos(List<AgentChatSessionEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        List<AgentSessionDto> list = new ArrayList<>(entities.size());
        for (AgentChatSessionEntity entity : entities) {
            list.add(toSessionDto(entity));
        }
        return list;
    }

    private List<AgentMessageDto> toMessageDtos(List<AgentChatMessageEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        List<AgentMessageDto> list = new ArrayList<>(entities.size());
        for (AgentChatMessageEntity entity : entities) {
            list.add(toMessageDto(entity));
        }
        return list;
    }
}
