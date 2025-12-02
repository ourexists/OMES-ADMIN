/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.portal.auth.captcha;

import com.ourexists.era.oauth2.core.token.EraAuthenticationToken;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;

import java.util.Collection;

/**
 * @author pengcheng
 * @date 2022/4/14 16:53
 * @since 1.0.0
 */
@Getter
public class CaptchaAuthenticationToken extends EraAuthenticationToken {

    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    private String username;

    private String password;
    private String captcha;

    public CaptchaAuthenticationToken(String clientId, String username, String password, String captcha) {
        super(username, password, clientId);
        this.username = username;
        this.password = password;
        this.captcha = captcha;
    }

    public CaptchaAuthenticationToken(String clientId, String username, String password, String captcha, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, clientId,authorities);
        this.username = username;
        this.password = password;
        this.captcha = captcha;
    }
}
