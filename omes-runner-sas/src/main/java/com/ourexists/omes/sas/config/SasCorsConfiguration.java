/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.sas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 前后端分离：允许独立部署的前端跨域访问 SAS 网关 OAuth2 / 业务 API。
 */
@Configuration
@EnableConfigurationProperties(SasCorsConfiguration.SasCorsProperties.class)
public class SasCorsConfiguration {

    @Bean
    @Primary
    public CorsConfigurationSource corsConfigurationSource(SasCorsProperties properties) {
        CorsConfiguration config = buildCorsConfiguration(properties);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    static CorsConfiguration buildCorsConfiguration(SasCorsProperties properties) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        config.setMaxAge(3600L);
        List<String> origins = properties.getAllowedOriginPatterns();
        if (origins == null || origins.isEmpty()) {
            config.addAllowedOriginPattern("*");
            config.addAllowedOriginPattern("http://127.0.0.1:*");
            config.addAllowedOriginPattern("http://localhost:*");
            config.addAllowedOriginPattern("http://192.168.*.*:*");
            config.addAllowedOriginPattern("http://10.*.*.*:*");
            config.addAllowedOriginPattern("http://172.*.*.*:*");
        } else {
            origins.forEach(config::addAllowedOriginPattern);
        }
        return config;
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
