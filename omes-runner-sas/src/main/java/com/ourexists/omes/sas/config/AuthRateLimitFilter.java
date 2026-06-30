/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.sas.config;

import com.ourexists.omes.sas.auth.AuthEndpointRateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private final AuthEndpointRateLimiter rateLimiter;

    public AuthRateLimitFilter(AuthEndpointRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getServletPath();
        String clientKey = resolveClientKey(request);

        if (path.startsWith("/open/captcha") && !rateLimiter.allowCaptcha(clientKey)) {
            writeTooManyRequests(response);
            return;
        }
        if ("/oauth2/token".equals(path) && "POST".equalsIgnoreCase(request.getMethod())
                && !rateLimiter.allowToken(clientKey)) {
            writeTooManyRequests(response);
            return;
        }
        if (path.startsWith("/authentication/register") && "POST".equalsIgnoreCase(request.getMethod())
                && !rateLimiter.allowRegister(clientKey)) {
            writeTooManyRequests(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private static void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":429,\"msg\":\"请求过于频繁，请稍后再试\"}");
    }

    static String resolveClientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
