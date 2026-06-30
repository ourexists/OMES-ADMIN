/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.portal.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "omes.internal")
@Getter
@Setter
public class OmesInternalServiceProperties {

    public static final String HEADER = "x-omes-internal-key";

    private String serviceKey = "";
}
