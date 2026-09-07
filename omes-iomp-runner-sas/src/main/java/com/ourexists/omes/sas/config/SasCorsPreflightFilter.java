/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.sas.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 在 Spring Security 之前处理 CORS 预检，避免 OPTIONS 未带 Access-Control-Allow-Origin。
 * 前后端分离时，独立部署的前端跨域访问 SAS 网关必需。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SasCorsPreflightFilter extends OncePerRequestFilter {

    private static final String ALLOW_METHODS = "GET,POST,PUT,DELETE,OPTIONS,HEAD,PATCH";
    private static final String EXPOSE_HEADERS = "Authorization, Content-Disposition";

    private final SasCorsConfiguration.SasCorsProperties corsProperties;

    public SasCorsPreflightFilter(SasCorsConfiguration.SasCorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (!StringUtils.hasText(origin) || !isAllowedOrigin(origin)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        response.setHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, EXPOSE_HEADERS);

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            String requestHeaders = request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, ALLOW_METHODS);
            response.setHeader(
                    HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                    StringUtils.hasText(requestHeaders) ? requestHeaders : "*"
            );
            response.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowedOrigin(String origin) {
        CorsConfiguration config = SasCorsConfiguration.buildCorsConfiguration(corsProperties);
        return config.checkOrigin(origin) != null;
    }
}
