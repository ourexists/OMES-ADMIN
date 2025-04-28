/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.auth;

import com.ourexists.era.framework.oauth2.AuthConstants;
import com.ourexists.era.framework.oauth2.handler.EraAuthenticationEntryPoint;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.AntPathMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MSEAuthExceptionEntryPoint implements EraAuthenticationEntryPoint {

    private final AntPathMatcher antPathMatcher;

    public MSEAuthExceptionEntryPoint(AntPathMatcher antPathMatcher) {
        this.antPathMatcher = antPathMatcher;
    }

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        boolean isRedic = true;
        for (String s : AuthConstants.SYSTEM_WHITE_PATH) {
            if (antPathMatcher.match(s, httpServletRequest.getServletPath())) {
                isRedic = false;
                break;
            }
        }
        if(isRedic) {
            httpServletResponse.sendRedirect("/view/login");
        }
    }
}
