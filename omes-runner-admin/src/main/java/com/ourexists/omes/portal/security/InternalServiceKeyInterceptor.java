/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.portal.security;

import com.ourexists.era.framework.core.constants.ResultMsgEnum;
import com.ourexists.era.framework.core.utils.EraStandardUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

public class InternalServiceKeyInterceptor implements HandlerInterceptor {

    private final OmesInternalServiceProperties properties;

    public InternalServiceKeyInterceptor(OmesInternalServiceProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String configuredKey = properties.getServiceKey();
        if (!StringUtils.hasText(configuredKey)) {
            EraStandardUtils.exceptionView(response, ResultMsgEnum.PERMISSION_DENIED, "internal service key not configured");
            return false;
        }
        String incomingKey = request.getHeader(OmesInternalServiceProperties.HEADER);
        if (!configuredKey.equals(incomingKey)) {
            EraStandardUtils.exceptionView(response, ResultMsgEnum.PERMISSION_DENIED, "invalid internal service key");
            return false;
        }
        return true;
    }
}
