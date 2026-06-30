/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.sas.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthEndpointRateLimiter {

    private static final String PREFIX = "omes:sas:rate:";

    private final StringRedisTemplate redisTemplate;

    @Value("${omes.auth-rate-limit.captcha-per-minute:60}")
    private int captchaPerMinute;

    @Value("${omes.auth-rate-limit.token-per-minute:30}")
    private int tokenPerMinute;

    @Value("${omes.auth-rate-limit.register-per-minute:10}")
    private int registerPerMinute;

    public AuthEndpointRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean allowCaptcha(String clientKey) {
        return tryAcquire("captcha", clientKey, captchaPerMinute, Duration.ofMinutes(1));
    }

    public boolean allowToken(String clientKey) {
        return tryAcquire("token", clientKey, tokenPerMinute, Duration.ofMinutes(1));
    }

    public boolean allowRegister(String clientKey) {
        return tryAcquire("register", clientKey, registerPerMinute, Duration.ofMinutes(1));
    }

    private boolean tryAcquire(String scope, String key, int maxRequests, Duration window) {
        if (maxRequests <= 0) {
            return true;
        }
        String redisKey = PREFIX + scope + ":" + key;
        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count != null && count == 1L) {
            redisTemplate.expire(redisKey, window);
        }
        return count != null && count <= maxRequests;
    }
}
