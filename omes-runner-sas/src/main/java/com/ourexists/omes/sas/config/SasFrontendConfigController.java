/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.sas.config;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Same-origin gateway entry: OAuth2/captcha use relative paths (empty sasBaseUrl).
 */
@RestController
@RequestMapping("/open/frontend-config")
public class SasFrontendConfigController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> config() {
        return Map.of("sasBaseUrl", "", "gatewayPort", 9400);
    }
}
