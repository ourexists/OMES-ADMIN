/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.auth;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author pengcheng
 * @version 1.0.0
 * @date 2023/4/14 15:26
 * @since 1.0.0
 */
@Component
public class AuthValidRuleCache {

    public static final Integer ACCOUNT_WRONG_LOCK_DURATION_HOUR = 24;

    public static final Integer CAPTCHA_EXPIRE_MINUTES = 3;

    private Cache<String, String> captchaCache;

    private Cache<String, Long> wrongCache;


    public AuthValidRuleCache() {
        captchaCache = CacheBuilder.newBuilder().expireAfterWrite(CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES).build();
        wrongCache = CacheBuilder.newBuilder().expireAfterWrite(ACCOUNT_WRONG_LOCK_DURATION_HOUR, TimeUnit.HOURS).build();
    }

    public void setCaptcha(String uuid, String code) {
        captchaCache.put(uuid, code);
    }

    public String getCaptcha(String uuid) {
        return captchaCache.getIfPresent(uuid);
    }

    public void removeCaptcha(String uuid) {
        captchaCache.invalidate(uuid);
    }

    public long accumulatesError(String accname) {
        Long count = wrongCache.getIfPresent(accname);
        if (count == null) {
            count = 0L;
        }
        wrongCache.put(accname, count++);
        return count;
    }

    public long wrongNum(String accname) {
        Long count = wrongCache.getIfPresent(accname);
        return count == null ? 0L : count;
    }


}
