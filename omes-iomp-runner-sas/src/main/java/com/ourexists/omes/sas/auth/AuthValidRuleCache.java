/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.sas.auth;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class AuthValidRuleCache {

    public static final Integer ACCOUNT_WRONG_LOCK_DURATION_HOUR = 24;

    public static final Integer CAPTCHA_EXPIRE_MINUTES = 3;

    private static final String CAPTCHA_PREFIX = "omes:sas:captcha:";

    private static final String WRONG_PREFIX = "omes:sas:login-wrong:";

    private final StringRedisTemplate redisTemplate;

    public AuthValidRuleCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setCaptcha(String uuid, String code) {
        redisTemplate.opsForValue().set(
                CAPTCHA_PREFIX + uuid,
                code,
                CAPTCHA_EXPIRE_MINUTES,
                TimeUnit.MINUTES);
    }

    public String getCaptcha(String uuid) {
        return redisTemplate.opsForValue().get(CAPTCHA_PREFIX + uuid);
    }

    public void removeCaptcha(String uuid) {
        redisTemplate.delete(CAPTCHA_PREFIX + uuid);
    }

    public long accumulatesError(String accname) {
        String key = WRONG_PREFIX + accname;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, ACCOUNT_WRONG_LOCK_DURATION_HOUR, TimeUnit.HOURS);
        }
        return count == null ? 0L : count;
    }

    public long wrongNum(String accname) {
        String value = redisTemplate.opsForValue().get(WRONG_PREFIX + accname);
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    public void clearWrongNum(String accname) {
        redisTemplate.delete(WRONG_PREFIX + accname);
    }
}
