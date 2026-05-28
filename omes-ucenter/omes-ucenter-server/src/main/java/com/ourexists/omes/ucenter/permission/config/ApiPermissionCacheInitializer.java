/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.ucenter.permission.config;

import com.ourexists.omes.ucenter.permission.service.ApiPermissionCacheService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApiPermissionCacheInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final ApiPermissionCacheService apiPermissionCacheService;

    public ApiPermissionCacheInitializer(ApiPermissionCacheService apiPermissionCacheService) {
        this.apiPermissionCacheService = apiPermissionCacheService;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        apiPermissionCacheService.refresh();
    }
}
