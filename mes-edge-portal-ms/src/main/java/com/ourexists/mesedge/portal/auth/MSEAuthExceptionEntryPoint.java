/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.auth;

import com.ourexists.era.framework.core.PathRule;
import com.ourexists.era.framework.core.constants.ResultMsgEnum;
import com.ourexists.era.framework.core.utils.EraStandardUtils;
import com.ourexists.era.framework.webserver.enhance.I18nUtil;
import com.ourexists.era.oauth2.core.handler.EraAuthenticationEntryPoint;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

@Slf4j
public class MSEAuthExceptionEntryPoint implements EraAuthenticationEntryPoint {

    private final AntPathMatcher antPathMatcher;

    public MSEAuthExceptionEntryPoint(AntPathMatcher antPathMatcher) {
        this.antPathMatcher = antPathMatcher;
    }

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        boolean isRedic = true;
        for (String s : PathRule.OAUTH_PATHS) {
            if (antPathMatcher.match(s, httpServletRequest.getServletPath())) {
                isRedic = false;
                break;
            }
        }
        if (isRedic) {
            log.error(e.getMessage(), e);
            httpServletResponse.setStatus(401);
            EraStandardUtils.exceptionView(httpServletResponse, ResultMsgEnum.SC_UNAUTHORIZED, I18nUtil.i18nParser(e.getMessage()));
        } else {
            log.error(e.getMessage(), e);
            httpServletResponse.setStatus(200);
            EraStandardUtils.exceptionView(httpServletResponse, ResultMsgEnum.SYSTEM_ERROR, I18nUtil.i18nParser(e.getMessage()));
        }
    }
}
