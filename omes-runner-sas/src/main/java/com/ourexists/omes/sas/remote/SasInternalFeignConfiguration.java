/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.sas.remote;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SasInternalFeignConfiguration {

    public static final String INTERNAL_SERVICE_HEADER = "x-omes-internal-key";

    @Bean
    public RequestInterceptor omesInternalServiceKeyInterceptor(
            @Value("${omes.internal.service-key:}") String serviceKey) {
        return template -> template.header(INTERNAL_SERVICE_HEADER, serviceKey);
    }
}
