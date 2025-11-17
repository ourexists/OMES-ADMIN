package com.ourexists.mesedge.portal.config;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;

@Component
public class CacheUtils {

    @Autowired
    private CacheManager cacheManager;

    public Cache<Object, Object> nativeCache(String cacheName) {
        CaffeineCache springCache = (CaffeineCache) cacheManager.getCache(cacheName);
        return springCache.getNativeCache();
    }

    public void put(String cacheName, Object key, Object value) {
        nativeCache(cacheName).put(key, value);
    }

    public <T> T get(String cacheName, Object key) {
        return (T) nativeCache(cacheName).getIfPresent(key);
    }

    public void evict(String cacheName, Object key) {
        nativeCache(cacheName).invalidate(key);
    }

    public void clear(String cacheName) {
        nativeCache(cacheName).invalidateAll();
    }
}
