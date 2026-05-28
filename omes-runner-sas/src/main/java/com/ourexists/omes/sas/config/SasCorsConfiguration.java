/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.sas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * SAS 独立部署时允许 Admin、H5 等前端跨域访问 OAuth2 / 验证码接口。
 */
@Configuration
@EnableConfigurationProperties(SasCorsConfiguration.SasCorsProperties.class)
public class SasCorsConfiguration {

    @Bean
    public FilterRegistrationBean<CorsFilter> sasCorsFilter(SasCorsProperties properties) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setMaxAge(3600L);
        List<String> origins = properties.getAllowedOriginPatterns();
        if (origins == null || origins.isEmpty()) {
            config.addAllowedOriginPattern("http://127.0.0.1:*");
            config.addAllowedOriginPattern("http://localhost:*");
        } else {
            origins.forEach(config::addAllowedOriginPattern);
        }
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    @ConfigurationProperties(prefix = "omes.cors")
    public static class SasCorsProperties {
        private List<String> allowedOriginPatterns = new ArrayList<>();

        public List<String> getAllowedOriginPatterns() {
            return allowedOriginPatterns;
        }

        public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
            this.allowedOriginPatterns = allowedOriginPatterns;
        }
    }
}
