/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.ucenter.permission.mapper.RolePermissionMapper;
import com.ourexists.mesedge.ucenter.permission.pojo.RolePermission;
import com.ourexists.mesedge.ucenter.permission.service.RolePermissionService;
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
public class RolePermissionServiceImpl extends AbstractMyBatisPlusService<RolePermissionMapper, RolePermission>
        implements RolePermissionService {

    @Override
    public void establishRelation(String roleId, List<String> permissionIds) {
        this.removeRelationByRole(roleId);
        if (CollectionUtils.isEmpty(permissionIds)) {
            return;
        }
        List<RolePermission> rolePermissions = new ArrayList<>();
        for (String permissionId : permissionIds) {
            rolePermissions.add(new RolePermission().setRoleId(roleId).setPermissionId(permissionId));
        }
        this.saveBatch(rolePermissions);
    }

    @Override
    public void removeRelationByPermission(String permissionId) {
        this.remove(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getPermissionId, permissionId));
    }

    @Override
    public void removeRelationByRole(String roleId) {
        this.remove(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
    }
}
