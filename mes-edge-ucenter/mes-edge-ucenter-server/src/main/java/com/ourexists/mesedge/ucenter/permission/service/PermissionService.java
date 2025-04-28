/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.permission.service;


import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.ucenter.permission.PermissionDto;
import com.ourexists.mesedge.ucenter.permission.PermissionModifyDto;
import com.ourexists.mesedge.ucenter.permission.PermissionTreeNode;
import com.ourexists.mesedge.ucenter.permission.pojo.Permission;
import com.ourexists.mesedge.ucenter.permission.pojo.PermissionApi;

import java.util.List;
import java.util.Map;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface PermissionService extends IMyBatisPlusService<Permission> {

    /**
     * 新增部门
     *
     * @param userGroupDto
     */
    void add(PermissionDto userGroupDto);

    /**
     * 修改部门
     *
     * @param modifyDto
     */
    void modify(PermissionModifyDto modifyDto);

    /**
     * 删除部门
     *
     * @param id 部门id
     */
    void removeById(String id);

    /**
     * 查询权限树
     *
     * @param parent 要查询的根
     * @return
     */
    PermissionTreeNode selectPermissionTree(Permission parent);


    /**
     * 查询租户所持有的权限
     *
     * @param tenantId 租户id
     * @return
     */
    List<Permission> selectPermissionWhichTenantHold(String tenantId);

    /**
     * 查询租户所持有的平台权限
     *
     * @param tenantId 租户id
     * @param platform 平台编号。可选
     * @return
     */
    List<Permission> selectPermissionWhichTenantHold(String tenantId, String platform);

    /**
     * 查询角色持有的权限
     *
     * @param roleId 角色id
     * @return
     */
    List<Permission> selectPermissionWhichRoleHold(String roleId);


    /**
     * 查询平台持有的权限
     *
     * @param platform 平台编号
     * @return
     */
    List<Permission> selectPermissionWhichPlatformHold(String platform);

    /**
     * 查询账户所持有的所有权限
     *
     * @param accId 账户Id,必传
     * @return
     */
    List<Permission> selectPermissionWhichAccHold(String accId);


    /**
     * 查询账户所持有的平台权限
     *
     * @param accId    账户Id,必传
     * @param platform 平台，可选
     * @return
     */
    List<Permission> selectPermissionWhichAccHold(String accId, String platform);

    /**
     * 查询角色持有的平台权限
     *
     * @param roleId   角色id,必传
     * @param platform 平台，可选
     * @return
     */
    List<Permission> selectPermissionWhichRoleHold(String roleId, String platform);

    /**
     * 查询出账户所有的权限
     *
     * @param accId
     * @return
     */
    Map<String, List<PermissionApi>> selectAccPermissionApiGroupByTenant(String accId);

}
