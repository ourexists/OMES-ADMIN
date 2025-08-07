/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.auth;

import com.ourexists.era.framework.core.user.TenantInfo;
import com.ourexists.era.framework.core.user.UserInfo;
import com.ourexists.era.oauth2.core.EraUser;
import com.ourexists.era.oauth2.core.authority.ApiPermission;
import com.ourexists.era.oauth2.core.authority.DefaultApiPermission;
import com.ourexists.mesedge.ucenter.account.AccVo;
import com.ourexists.mesedge.ucenter.enums.AccStatusEnum;
import com.ourexists.mesedge.ucenter.permission.PermissionApiDetailDto;
import com.ourexists.mesedge.ucenter.tenant.TenantUVo;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author pengcheng
 * @date 2022/4/15 18:48
 * @since 1.0.0
 */
public abstract class CommonUserDetailService {

//    @Autowired
//    private EraEventCollector eraEventCollector;

    protected Collection<? extends ApiPermission> apiPermissions(AccVo accVo) {
        List<ApiPermission> apiPermissions = new ArrayList<>();
        List<PermissionApiDetailDto> permissionApis = accVo.getPermissionApiDetailDtos();
        if (!CollectionUtils.isEmpty(permissionApis)) {
            for (PermissionApiDetailDto permissionApi : permissionApis) {
                apiPermissions.add(new DefaultApiPermission(permissionApi.getServerName(), permissionApi.getPath()));
            }
        }
        //查询
        return apiPermissions;
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
//                if (tenantVo.getManagement().equals(ManagementControlEnum.SERVER.getCode())) {
//                    TenantDataAuth tenantDataAuth = new TenantDataAuth();
//                    tenantDataAuth.addLowControlPower(OperatorModel.QUERY);
//                    tenantDataAuth.addLowControlPower(OperatorModel.DELETE);
//                    tenantDataAuth.addLowControlPower(OperatorModel.UPDATE);
//                    tenantInfo.setTenantDataAuth(tenantDataAuth);
//                }
                tenantInfoMap.put(tenantVo.getTenantCode(), tenantInfo);
            }
        }

        UserInfo userInfo = userInfo(account, detail);
        EraUser eraUser = new EraUser(userInfo, account.getPassword(), enabled,
                accountNonExpired, credentialsNonExpired, accountNonLocked, tenantInfoMap,
                apiPermissions(account),
                new ArrayList<>());
        postProcessor(eraUser);
        return eraUser;
    }

    protected void postProcessor(EraUser eraUser) {
//        if (eraUser.isEnabled() && eraUser.isAccountNonExpired() && eraUser.isCredentialsNonExpired()
//                && eraUser.isAccountNonLocked()) {
//            eraEventCollector.doCollect(
//                    new Event()
//                            .setEventCode(this.getClass().getSimpleName().replace("DetailsServiceImpl", ""))
//                            .setEventTypeEnum(EventTypeEnum.LOGIN)
//                            .setCreateBy(eraUser.getUserInfo().getUsername())
//            );
//        }
    }

    protected EraUser eraUser(AccVo account) {
        return eraUser(account, null);
    }
}
