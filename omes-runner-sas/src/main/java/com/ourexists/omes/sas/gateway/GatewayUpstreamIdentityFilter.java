/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.sas.gateway;

import com.ourexists.era.framework.core.EraSystemHeader;
import com.ourexists.era.oauth2.core.EraUser;
import com.ourexists.era.oauth2.core.gateway.GatewayIdentityHeaders;
import com.ourexists.era.oauth2.core.gateway.GatewayIdentitySupport;
import com.ourexists.era.oauth2.core.interceptor.EraUserResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Validates Bearer JWT for upstream Admin routes and injects trusted internal identity headers.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
public class GatewayUpstreamIdentityFilter extends OncePerRequestFilter {

    private static final Set<String> STRIPPED_ON_PROXY = Set.of(
            EraSystemHeader.AUTH_HEADER.toLowerCase()
    );

    private final OmesGatewayProperties gatewayProperties;
    private final GatewayLocalPathMatcher localPathMatcher;
    private final JwtDecoder jwtDecoder;
    private final EraUserResolver eraUserResolver;
    private final String internalServiceKey;

    public GatewayUpstreamIdentityFilter(OmesGatewayProperties gatewayProperties,
                                         GatewayLocalPathMatcher localPathMatcher,
                                         JwtDecoder jwtDecoder,
                                         EraUserResolver eraUserResolver,
                                         @Value("${omes.internal.service-key:}") String internalServiceKey) {
        this.gatewayProperties = gatewayProperties;
        this.localPathMatcher = localPathMatcher;
        this.jwtDecoder = jwtDecoder;
        this.eraUserResolver = eraUserResolver;
        this.internalServiceKey = internalServiceKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!gatewayProperties.isEnabled() || localPathMatcher.isLocalPath(request, gatewayProperties)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader(EraSystemHeader.AUTH_HEADER);
        if (!StringUtils.hasText(authorization) || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            filterChain.doFilter(request, response);
            return;
        }

        String tokenValue = authorization.substring(7).trim();
        if (!StringUtils.hasText(tokenValue)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "invalid bearer token");
            return;
        }

        try {
            Jwt jwt = jwtDecoder.decode(tokenValue);
            // Single-arg JwtAuthenticationToken leaves authenticated=false; use authorities ctor after decode.
            EraUser eraUser = eraUserResolver.resolve(new JwtAuthenticationToken(jwt, Collections.emptyList()));
            if (eraUser == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "user context not found");
                return;
            }

            Map<String, String> propagated = new LinkedHashMap<>(
                    GatewayIdentitySupport.buildPropagationHeaders(eraUser, EraSystemHeader.extractTenantId(request)));
            if (StringUtils.hasText(internalServiceKey)) {
                propagated.put(GatewayIdentityHeaders.INTERNAL_SERVICE_KEY, internalServiceKey);
            }
            filterChain.doFilter(new PropagatedIdentityRequest(request, propagated), response);
        } catch (JwtException ex) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "invalid bearer token");
        }
    }

    private static final class PropagatedIdentityRequest extends jakarta.servlet.http.HttpServletRequestWrapper {

        private final Map<String, String> propagatedHeaders;

        private PropagatedIdentityRequest(HttpServletRequest request, Map<String, String> propagatedHeaders) {
            super(request);
            this.propagatedHeaders = propagatedHeaders;
        }

        @Override
        public String getHeader(String name) {
            if (name != null && STRIPPED_ON_PROXY.contains(name.toLowerCase())) {
                return null;
            }
            if (name != null && propagatedHeaders.containsKey(name)) {
                return propagatedHeaders.get(name);
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (name != null && STRIPPED_ON_PROXY.contains(name.toLowerCase())) {
                return Collections.emptyEnumeration();
            }
            if (name != null && propagatedHeaders.containsKey(name)) {
                return Collections.enumeration(Collections.singletonList(propagatedHeaders.get(name)));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> names = new HashSet<>();
            Enumeration<String> original = super.getHeaderNames();
            while (original.hasMoreElements()) {
                String name = original.nextElement();
                if (!STRIPPED_ON_PROXY.contains(name.toLowerCase())) {
                    names.add(name);
                }
            }
            names.addAll(propagatedHeaders.keySet());
            return Collections.enumeration(names);
        }
    }
}
