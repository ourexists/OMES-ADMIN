/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.auth;

import com.ourexists.era.framework.core.PathRule;
import com.ourexists.era.oauth2.core.handler.EraAuthenticationEntryPoint;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

public class MSEAuthExceptionEntryPoint implements EraAuthenticationEntryPoint {

    private final AntPathMatcher antPathMatcher;

    public MSEAuthExceptionEntryPoint(AntPathMatcher antPathMatcher) {
        this.antPathMatcher = antPathMatcher;
    }

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        boolean isRedic = true;
        for (String s : PathRule.SYSTEM_WHITE_PATH) {
            if (antPathMatcher.match(s, httpServletRequest.getServletPath())) {
                isRedic = false;
                break;
            }
        }
        if (isRedic) {
            httpServletResponse.sendRedirect("/view/login");
        }
    }
}
