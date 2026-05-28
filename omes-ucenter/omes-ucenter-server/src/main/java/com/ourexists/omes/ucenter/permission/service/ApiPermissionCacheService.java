/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.ucenter.permission.service;

import com.ourexists.era.oauth2.core.authority.ApiPermission;
import com.ourexists.era.oauth2.core.authority.DefaultApiPermission;
import com.ourexists.era.oauth2.core.store.PermissionStore;
import com.ourexists.omes.ucenter.permission.pojo.PermissionApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 将数据库中的 API 权限定义同步到 {@link PermissionStore}，供 ERA OAuth2 RBAC 拦截器校验。
 */
@Slf4j
@Service
public class ApiPermissionCacheService {

    private final ObjectProvider<PermissionStore> permissionStoreProvider;
    private final PermissionApiService permissionApiService;

    public ApiPermissionCacheService(ObjectProvider<PermissionStore> permissionStoreProvider,
                                     PermissionApiService permissionApiService) {
        this.permissionStoreProvider = permissionStoreProvider;
        this.permissionApiService = permissionApiService;
    }

    public void refresh() {
        PermissionStore permissionStore = permissionStoreProvider.getIfAvailable();
        if (permissionStore == null) {
            log.debug("PermissionStore not available, skip API permission cache refresh");
            return;
        }
        List<PermissionApi> permissionApis = permissionApiService.list();
        Collection<ApiPermission> apiPermissions = new ArrayList<>();
        if (permissionApis != null) {
            for (PermissionApi permissionApi : permissionApis) {
                if (!StringUtils.hasText(permissionApi.getServerName())
                        || !StringUtils.hasText(permissionApi.getPath())) {
                    continue;
                }
                apiPermissions.add(new DefaultApiPermission(
                        permissionApi.getServerName().trim(),
                        permissionApi.getPath().trim()));
            }
        }
        permissionStore.setPermission(apiPermissions);
        log.info("Refreshed global API permission cache, size={}", apiPermissions.size());
    }
}
