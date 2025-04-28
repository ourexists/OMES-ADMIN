/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.permission.service;


import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.ucenter.permission.pojo.TenantPermission;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface TenantPermissionService extends IMyBatisPlusService<TenantPermission> {

    /**
     * 建立关联
     *
     * @param tenantId
     * @param permissionIds
     */
    void establishRelation(String tenantId, List<String> permissionIds);

    /**
     * 移除关联
     *
     * @param permissionId
     */
    void removeRelationByPermission(String permissionId);

    /**
     * 移除关联
     *
     * @param roleId
     */
    void removeRelationByRole(String roleId);
}
