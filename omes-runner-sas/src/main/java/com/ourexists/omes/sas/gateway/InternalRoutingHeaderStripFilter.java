/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.sas.gateway;

import com.ourexists.era.framework.core.EraSystemHeader;
import com.ourexists.era.oauth2.core.gateway.GatewayIdentityHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Strips internal routing identity headers from inbound client requests (same as ERA gateway filter).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class InternalRoutingHeaderStripFilter extends OncePerRequestFilter {

    private static final Set<String> STRIPPED = Set.of(
            EraSystemHeader.AUTH_CONTRO_USER_HEADER.toLowerCase(),
            EraSystemHeader.AUTH_CONTRO_ROLE_HEADER.toLowerCase(),
            EraSystemHeader.AUTH_CONTRO_DATA_AUTH_HEADER.toLowerCase(),
            EraSystemHeader.AUTH_CONTRO_SKIPMAIN.toLowerCase(),
            GatewayIdentityHeaders.ERA_USER.toLowerCase(),
            GatewayIdentityHeaders.INTERNAL_SERVICE_KEY.toLowerCase()
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(new StrippedHeaderRequest(request), response);
    }

    private static final class StrippedHeaderRequest extends HttpServletRequestWrapper {

        private StrippedHeaderRequest(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getHeader(String name) {
            if (name != null && STRIPPED.contains(name.toLowerCase())) {
                return null;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (name != null && STRIPPED.contains(name.toLowerCase())) {
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
                if (!STRIPPED.contains(name.toLowerCase())) {
                    names.add(name);
                }
            }
            return Collections.enumeration(names);
        }
    }
}
