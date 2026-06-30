/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.process.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ProcessFileAccessService {

    private final ProcessFileProperties fileProperties;

    public String buildAccessUrlByStoragePath(String storagePath) {
        if (!isStoragePathRef(storagePath)) {
            return null;
        }
        String path = storagePath.trim();
        String prefix = fileProperties.getUrlPrefix();
        if (!StringUtils.hasText(prefix)) {
            prefix = "/localFile/download?path=";
        }
        if (prefix.contains("?")) {
            return prefix + urlEncode(path);
        }
        String normalized = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
        return normalized + "/" + path;
    }

    public boolean isStoragePathRef(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("data:") || trimmed.contains("base64,")) {
            return false;
        }
        if (trimmed.startsWith("blob:")) {
            return false;
        }
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return false;
        }
        if (trimmed.contains("..") || trimmed.length() > 512) {
            return false;
        }
        return !trimmed.startsWith("/");
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
