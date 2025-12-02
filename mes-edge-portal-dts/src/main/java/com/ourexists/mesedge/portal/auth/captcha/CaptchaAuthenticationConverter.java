/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.portal.auth.captcha;

import com.ourexists.era.framework.webserver.authorization.EraAuthenticationConverter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

/**
 * @author pengcheng
 * {@code @date 2025/5/14 16:58}
 * @since 1.0.0
 */
public class CaptchaAuthenticationConverter implements EraAuthenticationConverter {

    public static final String GRANT_TYPE = "captcha";

    private static final String PARAMETER_CAPTCHA = "captcha";

    @Override
    public Authentication convert(HttpServletRequest request) {
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!GRANT_TYPE.equals(grantType)) {
            return null;
        }
        String clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
        String username = request.getParameter(OAuth2ParameterNames.USERNAME);
        String password = request.getParameter(OAuth2ParameterNames.PASSWORD);
        String captcha = request.getParameter(PARAMETER_CAPTCHA);
        return new CaptchaAuthenticationToken(clientId, username, password, captcha);
    }
}
