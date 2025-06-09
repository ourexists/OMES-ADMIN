/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.portal.auth;

import com.ourexists.era.framework.oauth2.handler.EraAuthenticationEntryPoint;
import com.ourexists.mesedge.portal.auth.captcha.CaptchaTokenGranter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.client.ClientCredentialsTokenGranter;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeTokenGranter;
import org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.implicit.ImplicitTokenGranter;
import org.springframework.security.oauth2.provider.password.ResourceOwnerPasswordTokenGranter;
import org.springframework.security.oauth2.provider.refresh.RefreshTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/14 17:44
 * @since 1.0.0
 */
@Configuration
@ConditionalOnClass(AuthorizationServerEndpointsConfiguration.class)
@Import(GrantWebSecurityConfigurer.class)
public class GrantConfiguration {

    private final AuthorizationServerEndpointsConfigurer authorizationServerEndpointsConfigurer;

    private TokenGranter tokenGranter;

    private final ClientDetailsService clientDetailsService;

    private final AuthorizationServerTokenServices authorizationServerTokenServices;

    private final AuthorizationCodeServices authorizationCodeServices;

    private final AuthenticationManager authenticationManager;

    private final OAuth2RequestFactory oAuth2RequestFactory;

    public GrantConfiguration(
            AuthorizationServerEndpointsConfiguration authorizationServerEndpointsConfiguration,
            AuthenticationManager authenticationManager,
            AuthorizationEndpoint authorizationEndpoint,
            TokenEndpoint tokenEndpoint) {
        this.tokenGranter = tokenGranter();
        authorizationEndpoint.setTokenGranter(this.tokenGranter);
        tokenEndpoint.setTokenGranter(this.tokenGranter);
        this.authorizationServerEndpointsConfigurer = authorizationServerEndpointsConfiguration.getEndpointsConfigurer();
        this.authorizationCodeServices = this.authorizationServerEndpointsConfigurer.getAuthorizationCodeServices();
        this.oAuth2RequestFactory = this.authorizationServerEndpointsConfigurer.getOAuth2RequestFactory();
        this.clientDetailsService = this.authorizationServerEndpointsConfigurer.getClientDetailsService();
        this.authorizationServerTokenServices = this.authorizationServerEndpointsConfigurer.getDefaultAuthorizationServerTokenServices();
        this.authenticationManager = authenticationManager;
        this.authorizationServerEndpointsConfigurer.tokenGranter(this.tokenGranter);
    }

    private TokenGranter tokenGranter() {
        if (tokenGranter == null) {
            tokenGranter = new TokenGranter() {
                private CompositeTokenGranter delegate;

                @Override
                public OAuth2AccessToken grant(String grantType, TokenRequest tokenRequest) {
                    if (delegate == null) {
                        delegate = new CompositeTokenGranter(getDefaultTokenGranters());
                    }
                    return delegate.grant(grantType, tokenRequest);
                }
            };
        }
        return tokenGranter;
    }

    private List<TokenGranter> getDefaultTokenGranters() {
        ClientDetailsService clientDetails = this.clientDetailsService;
        AuthorizationServerTokenServices tokenServices = this.authorizationServerTokenServices;
        AuthorizationCodeServices authorizationCodeServices = this.authorizationCodeServices;
        OAuth2RequestFactory requestFactory = this.oAuth2RequestFactory;

        List<TokenGranter> tokenGranters = new ArrayList<>();
        tokenGranters.add(new AuthorizationCodeTokenGranter(tokenServices, authorizationCodeServices, clientDetails,
                requestFactory));
        tokenGranters.add(new RefreshTokenGranter(tokenServices, clientDetails, requestFactory));
        ImplicitTokenGranter implicit = new ImplicitTokenGranter(tokenServices, clientDetails, requestFactory);
        tokenGranters.add(implicit);
        tokenGranters.add(new ClientCredentialsTokenGranter(tokenServices, clientDetails, requestFactory));
        if (authenticationManager != null) {
            tokenGranters.add(new ResourceOwnerPasswordTokenGranter(authenticationManager, tokenServices,
                    clientDetails, requestFactory));
            tokenGranters.add(new CaptchaTokenGranter(authenticationManager, tokenServices,
                    clientDetails, requestFactory));
        }
        return tokenGranters;
    }

    @Bean
    public EraAuthenticationEntryPoint eraAuthenticationEntryPoint(AntPathMatcher antPathMatcher) {
        return new MSEAuthExceptionEntryPoint(antPathMatcher);
    }
}
