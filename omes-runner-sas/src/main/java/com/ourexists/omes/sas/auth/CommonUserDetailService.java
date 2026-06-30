/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.sas.auth;

import com.ourexists.era.framework.core.user.TenantInfo;
import com.ourexists.era.framework.core.user.UserInfo;
import com.ourexists.era.oauth2.core.EraUser;
import com.ourexists.omes.ucenter.account.AccVo;
import com.ourexists.omes.ucenter.enums.AccStatusEnum;
import com.ourexists.omes.ucenter.tenant.TenantUVo;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author pengcheng
 * @date 2022/4/15 18:48
 * @since 1.0.0
 */
public abstract class CommonUserDetailService {

    protected Set<String> scopes(AccVo accVo) {
        if (accVo.getOauthScopes() == null || accVo.getOauthScopes().isEmpty()) {
            return Set.of();
        }
        Set<String> scopes = new LinkedHashSet<>();
        for (String scope : accVo.getOauthScopes()) {
            if (StringUtils.hasText(scope)) {
                scopes.add(scope.trim());
            }
        }
        return scopes;
    }

    protected UserInfo userInfo(AccVo account, String detail) {
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(account, userInfo, "detail");
        userInfo.setDetails(detail);
        return userInfo;
    }

    protected EraUser eraUser(AccVo account, String detail) {
        boolean enabled = true;
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;

        if (account.getExpireTime().getTime() < System.currentTimeMillis()) {
            accountNonExpired = false;
        }

        AccStatusEnum accStatusEnum = AccStatusEnum.valueof(account.getStatus());
        switch (accStatusEnum) {
            case FROZEN:
                accountNonLocked = false;
                break;
            case INVALID:
                enabled = false;
                break;
            default:
        }


        List<TenantUVo> tenantVos = account.getTenantVos();
        Map<String, TenantInfo> tenantInfoMap = new HashMap<>(16);
        if (CollectionUtils.isEmpty(tenantVos)) {
            enabled = false;
        } else {
            for (TenantUVo tenantVo : tenantVos) {
                TenantInfo tenantInfo = new TenantInfo()
                        .setTenantId(tenantVo.getTenantCode())
                        .setRole(tenantVo.getRole())
                        .setManagementControl(tenantVo.getManagement());
                tenantInfoMap.put(tenantVo.getTenantCode(), tenantInfo);
            }
        }

        UserInfo userInfo = userInfo(account, detail);
        EraUser eraUser = new EraUser(userInfo, account.getPassword(), enabled,
                accountNonExpired, credentialsNonExpired, accountNonLocked, tenantInfoMap,
                scopes(account),
                new ArrayList<>());
        postProcessor(eraUser);
        return eraUser;
    }

    protected void postProcessor(EraUser eraUser) {
    }

    protected EraUser eraUser(AccVo account) {
        return eraUser(account, null);
    }
}
