/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.sas.gateway;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "omes.gateway")
@Getter
@Setter
public class OmesGatewayProperties {

    /**
     * When enabled, non-SAS paths are proxied to OMES-ADMIN.
     */
    private boolean enabled = true;

    /**
     * OMES-ADMIN upstream base URL, e.g. http://127.0.0.1:10010
     */
    private String adminUrl = "http://127.0.0.1:10010";

    /**
     * Ant-style patterns handled locally by SAS (not proxied).
     */
    private String[] localPaths = {
            "/oauth2/**",
            "/open/**",
            "/.well-known/**",
            "/authentication/**",
            "/actuator/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/message/ws"
    };
}
