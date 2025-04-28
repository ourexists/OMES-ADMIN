/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.ucenter.permission.mapper.TenantPermissionMapper;
import com.ourexists.mesedge.ucenter.permission.pojo.TenantPermission;
import com.ourexists.mesedge.ucenter.permission.service.TenantPermissionService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:20
 * @since 1.0.0
 */
@Service
public class TenantPermissionServiceImpl extends AbstractMyBatisPlusService<TenantPermissionMapper, TenantPermission>
        implements TenantPermissionService {

    @Override
    public void establishRelation(String tenantId, List<String> permissionIds) {
        this.removeRelationByRole(tenantId);
        if (CollectionUtils.isEmpty(permissionIds)) {
            return;
        }
        List<TenantPermission> tenantPermissions = new ArrayList<>();
        for (String permissionId : permissionIds) {
            tenantPermissions.add(new TenantPermission().setTenantId(tenantId).setPermissionId(permissionId));
        }
        this.saveBatch(tenantPermissions);
    }

    @Override
    public void removeRelationByPermission(String permissionId) {
        this.remove(new LambdaQueryWrapper<TenantPermission>().eq(TenantPermission::getPermissionId, permissionId));
    }

    @Override
    public void removeRelationByRole(String tenantId) {
        this.remove(new LambdaQueryWrapper<TenantPermission>().eq(TenantPermission::getTenantId, tenantId));
    }
}
