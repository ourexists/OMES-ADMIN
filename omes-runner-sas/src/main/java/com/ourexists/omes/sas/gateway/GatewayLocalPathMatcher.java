/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.sas.gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
public class GatewayLocalPathMatcher {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public boolean isLocalPath(HttpServletRequest request, OmesGatewayProperties properties) {
        String path = request.getServletPath();
        for (String pattern : properties.getLocalPaths()) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }
}
