/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.tenant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.ucenter.enums.TenantStatusEnum;
import com.ourexists.mesedge.ucenter.tenant.*;
import com.ourexists.mesedge.ucenter.tenant.pojo.Tenant;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface TenantService extends IMyBatisPlusService<Tenant> {

    /**
     * 入驻
     *
     * @param tenantSettledDto 入驻信息
     */
    void settled(TenantSettledDto tenantSettledDto);

    /**
     * 用户归属的租户
     *
     * @param userId 用户id
     * @return 用户归属的所有租户信息
     */
    List<Tenant> tenantToWhichTheUserBelong(String userId);


    /**
     * 用户归属的可用租户
     *
     * @param userId 用户id
     * @return 用户归属的在实际使用中的租户信息
     */
    List<Tenant> availableTenantToWhichTheUserBelong(String userId);


    /**
     * 用户归属的可用租户
     *
     * @param accId 用户id
     * @return
     */
    List<TenantUVo> availableTenantRoleWhichTheAccBelong(String accId);

    /**
     * 用户归属的可用租户
     *
     * @param userIds 用户id
     * @return
     */
    List<TenantUVo> availableTenantToWhichTheUsersBelong(List<String> userIds);

    /**
     * 修改租户状态
     *
     * @param tenantId         租户id
     * @param tenantStatusEnum 状态枚举
     */
    void modifyStatus(String tenantId, TenantStatusEnum tenantStatusEnum);

    /**
     * 修改信息
     *
     * @param tenantModifyDto 修改内容
     */
    void modify(TenantModifyDto tenantModifyDto);

    /**
     * 分页查询
     *
     * @param tenantPageQuery
     * @return
     */
    Page<Tenant> selectByPage(TenantPageQuery tenantPageQuery);

    List<Tenant> selectByIds(List<String> tenantIds);

    Tenant selectByCode(String code);


    List<Tenant> selectByCodes(List<String> codes);

    /**
     * 查询整个租户树
     *
     * @return
     */
    List<TenantTreeNode> selectTree();


    /**
     * 查询特定的租户树
     *
     * @param nodeCode 节点编号
     * @return 该特定节点的树
     */
    TenantTreeNode selectParticularTree(String nodeCode);

    List<Tenant> availableControllableTenantToTheCurrentBelong();
}
