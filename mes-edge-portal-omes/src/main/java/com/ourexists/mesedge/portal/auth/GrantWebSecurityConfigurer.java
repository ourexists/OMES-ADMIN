/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.auth;

import com.ourexists.era.oauth2.core.handler.EraAuthenticationEntryPoint;
import com.ourexists.era.oauth2.core.store.InMemoryPermissionStore;
import com.ourexists.era.oauth2.core.store.PermissionStore;
import com.ourexists.mesedge.portal.auth.captcha.CaptchaAuthenticationConverter;
import com.ourexists.mesedge.portal.auth.captcha.CaptchaAuthenticationProvider;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.AntPathMatcher;

@Configuration
@EnableWebSecurity
@Order(SecurityProperties.BASIC_AUTH_ORDER - 3)
public class GrantWebSecurityConfigurer {

    @Bean
    public CaptchaAuthenticationConverter captchaAuthenticationConverter() {
        return new CaptchaAuthenticationConverter();
    }

    @Bean
    public CaptchaAuthenticationProvider CaptchaAuthenticationProvider(UserDetailsService userDetailsService,
                                                                       AuthValidRuleCache authValidRuleCache,
                                                                       PasswordEncoder passwordEncoder,
                                                                       RegisteredClientRepository registeredClientRepository,
                                                                       OAuth2TokenGenerator<?> tokenGenerator,
                                                                       OAuth2AuthorizationService authorizationService) {
        return new CaptchaAuthenticationProvider(userDetailsService, authValidRuleCache, passwordEncoder, registeredClientRepository, tokenGenerator, authorizationService);
    }

    @Bean
    public EraAuthenticationEntryPoint eraAuthenticationEntryPoint() {
        return new MSEAuthExceptionEntryPoint(new AntPathMatcher());
    }

    @Bean
    public PermissionStore permissionStore() {
        return new InMemoryPermissionStore();
    }
}
