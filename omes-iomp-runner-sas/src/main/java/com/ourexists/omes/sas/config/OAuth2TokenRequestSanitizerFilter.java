/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.sas.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * 换 token 时不携带旧 Bearer / Session，避免 Resource Server 过滤器误判为已登录请求。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
public class OAuth2TokenRequestSanitizerFilter extends OncePerRequestFilter {

    private static final String TOKEN_PATH = "/oauth2/token";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isTokenRequest(request)) {
            filterChain.doFilter(new SanitizedTokenRequest(request), response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static boolean isTokenRequest(HttpServletRequest request) {
        return TOKEN_PATH.equals(request.getServletPath())
                && "POST".equalsIgnoreCase(request.getMethod());
    }

    private static final class SanitizedTokenRequest extends HttpServletRequestWrapper {

        private SanitizedTokenRequest(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getHeader(String name) {
            if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name)) {
                return null;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name)) {
                return Collections.emptyEnumeration();
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> names = new HashSet<>();
            Enumeration<String> original = super.getHeaderNames();
            while (original.hasMoreElements()) {
                String name = original.nextElement();
                if (!HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name)) {
                    names.add(name);
                }
            }
            return Collections.enumeration(names);
        }

        @Override
        public Cookie[] getCookies() {
            return null;
        }
    }
}
