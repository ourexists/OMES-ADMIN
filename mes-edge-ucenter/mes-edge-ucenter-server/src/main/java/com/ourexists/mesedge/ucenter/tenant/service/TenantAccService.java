/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.tenant.service;


import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.ucenter.enums.AccRoleEnum;
import com.ourexists.mesedge.ucenter.tenant.TenantAccDto;
import com.ourexists.mesedge.ucenter.tenant.pojo.TenantAcc;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface TenantAccService extends IMyBatisPlusService<TenantAcc> {

    /**
     * 建立关联
     *
     * @param accId            账户id
     * @param tenantAccDtoList 租户组
     */
    void establishRelation(String accId, List<TenantAccDto> tenantAccDtoList);

    /**
     * 建立关联
     *
     * @param accId    账户id
     * @param tenantId 租户id
     */
    void establishRelation(String accId, String tenantId, AccRoleEnum accRoleEnum);


    /**
     * 增量挂载
     *
     * @param tenantId
     * @param accRoleEnum
     * @param accIds
     */
    void incrementMount(String tenantId, AccRoleEnum accRoleEnum, List<String> accIds);


    /**
     * 新增关联
     *
     * @param accId    账户id
     * @param tenantId 租户id
     */
    void addRelation(String accId, String tenantId, AccRoleEnum accRoleEnum);


    /**
     * 新增关联
     *
     * @param tenantAccDtoList
     */
    void addRelation(List<TenantAccDto> tenantAccDtoList);

    /**
     * 解除关联
     *
     * @param accId    账户id
     * @param tenantId 租户id
     */
    void removeRelation(String accId, String tenantId);

    /**
     * 解除关联
     *
     * @param accId 账户id
     */
    void removeRelation(String accId);
}
