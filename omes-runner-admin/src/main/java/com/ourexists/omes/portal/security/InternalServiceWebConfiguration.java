/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.portal.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InternalServiceWebConfiguration implements WebMvcConfigurer {

    private final OmesInternalServiceProperties properties;

    public InternalServiceWebConfiguration(OmesInternalServiceProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InternalServiceKeyInterceptor interceptor = new InternalServiceKeyInterceptor(properties);
        registry.addInterceptor(interceptor)
                .addPathPatterns(
                        "/authentication/selectByAccname",
                        "/authentication/selectByMobile",
                        "/expose/**"
                )
                .order(0);
    }
}
