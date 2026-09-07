package com.ourexists.omes.sas.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Tag(name = "AI Bridge")
@RestController
@RequestMapping("/open/ai/bridge")
public class AiBridgeController {

    private static final String TICKET_PREFIX = "ai:bridge:ticket:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.bridge.ticket-ttl-seconds:120}")
    private long ticketTtlSeconds;

    @Value("${ai.bridge.internal-key:omes-bridge-internal}")
    private String internalKey;

    public AiBridgeController(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void requireInternalKey() {
        if (!StringUtils.hasText(internalKey)) {
            throw new IllegalStateException(
                    "Missing ai.bridge.internal-key. Configure AI_BRIDGE_INTERNAL_KEY before using AI bridge.");
        }
    }

    @Operation(summary = "签发 AI Bridge Ticket")
    @GetMapping("/issue")
    public JsonResponseEntity<Map<String, String>> issue(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestHeader(value = "x-era-platform", required = false) String platform,
            @RequestHeader(value = "x-route-tenant", required = false) String tenant,
            @RequestParam(value = "lang", required = false) String lang) throws Exception {
        Map<String, String> headers = new HashMap<>();
        if (authorization != null && !authorization.isBlank()) {
            headers.put(HttpHeaders.AUTHORIZATION, authorization);
        }
        if (platform != null && !platform.isBlank()) {
            headers.put("x-era-platform", platform);
        }
        if (tenant != null && !tenant.isBlank()) {
            headers.put("x-route-tenant", tenant);
        }
        if (lang != null && !lang.isBlank()) {
            headers.put("x-bridge-lang", lang);
        }
        String ticket = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(TICKET_PREFIX + ticket, objectMapper.writeValueAsString(headers), ticketTtlSeconds, TimeUnit.SECONDS);
        return JsonResponseEntity.success(Map.of("ticket", ticket, "expireSeconds", String.valueOf(ticketTtlSeconds)));
    }

    @Operation(summary = "解析 AI Bridge Ticket（一次性）")
    @GetMapping("/resolve")
    public JsonResponseEntity<Map<String, String>> resolve(
            @RequestParam("ticket") String ticket,
            @RequestHeader(value = "x-bridge-key", required = false) String incomingKey) throws Exception {
        if (incomingKey == null || !incomingKey.equals(internalKey)) {
            throw new BusinessException("forbidden");
        }
        String key = TICKET_PREFIX + ticket;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null || json.isBlank()) {
            throw new BusinessException("ticket expired");
        }
        redisTemplate.delete(key);
        Map<String, String> headers = objectMapper.readValue(json, new TypeReference<>() {
        });
        return JsonResponseEntity.success(headers);
    }
}
