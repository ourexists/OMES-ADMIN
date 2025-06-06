/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.auth;

import com.ourexists.mesedge.portal.auth.captcha.CaptchaAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Order(SecurityProperties.BASIC_AUTH_ORDER - 3)
public class GrantWebSecurityConfigurer extends WebSecurityConfigurerAdapter {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService userDetailService;

    @Autowired
    private AuthCacheManager authCacheManager;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Bean
    public TokenStore jwtTokenStore(JwtAccessTokenConverter jwtAccessTokenConverter) {
        return new JwtTokenStore(jwtAccessTokenConverter);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        CaptchaAuthenticationProvider captchaAuthenticationProvider = new CaptchaAuthenticationProvider(userDetailService, authCacheManager, passwordEncoder);
        http
                .cors().and()
                .csrf().disable()
                .addFilterBefore(new CorsFilter(corsConfigurationSource), WebAsyncManagerIntegrationFilter.class)
                .authenticationProvider(captchaAuthenticationProvider)
                // 基于token，所以不需要session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                //未匹配的任何请求都要认证
                .anyRequest()
                .authenticated();
        // 禁用缓存
        http.headers().cacheControl();
        http.headers().frameOptions().sameOrigin();
    }
}
