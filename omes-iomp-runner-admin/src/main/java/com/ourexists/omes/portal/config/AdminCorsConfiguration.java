/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.portal.config;

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
 * 开发环境直连 Admin 时的 CORS（生产环境前端经 SAS 跨域，见 SAS Cors 配置）。
 */
@Configuration
@EnableConfigurationProperties(AdminCorsConfiguration.AdminCorsProperties.class)
public class AdminCorsConfiguration {

    @Bean
    @Primary
    public CorsConfigurationSource corsConfigurationSource(AdminCorsProperties properties) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
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
        return source;
    }

    @ConfigurationProperties(prefix = "omes.cors")
    public static class AdminCorsProperties {
        private List<String> allowedOriginPatterns = new ArrayList<>();

        public List<String> getAllowedOriginPatterns() {
            return allowedOriginPatterns;
        }

        public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
            this.allowedOriginPatterns = allowedOriginPatterns;
        }
    }
}
