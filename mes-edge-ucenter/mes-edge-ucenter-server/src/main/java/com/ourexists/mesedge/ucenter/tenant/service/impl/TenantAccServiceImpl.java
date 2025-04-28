/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.tenant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.ucenter.enums.AccRoleEnum;
import com.ourexists.mesedge.ucenter.tenant.TenantAccDto;
import com.ourexists.mesedge.ucenter.tenant.mapper.TenantAccMapper;
import com.ourexists.mesedge.ucenter.tenant.pojo.TenantAcc;
import com.ourexists.mesedge.ucenter.tenant.service.TenantAccService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:20
 * @since 1.0.0
 */
@Service
public class TenantAccServiceImpl extends AbstractMyBatisPlusService<TenantAccMapper, TenantAcc>
        implements TenantAccService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void establishRelation(String accId, List<TenantAccDto> tenantAccDtoList) {
        this.removeRelation(accId);
        this.saveBatch(TenantAcc.warp(accId, tenantAccDtoList));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void establishRelation(String accId, String tenantId, AccRoleEnum accRole) {
        removeRelation(accId, tenantId);
        this.save(new TenantAcc().setAccId(accId).setTenantId(tenantId).setRole(accRole.name()));
    }

    @Override
    public void incrementMount(String tenantId, AccRoleEnum accRole, List<String> accIds) {
        try {
            List<TenantAcc> tenantAccs = new ArrayList<>();
            for (String accId : accIds) {
                tenantAccs.add(new TenantAcc().setAccId(accId).setTenantId(tenantId).setRole(accRole.name()));
            }
            this.saveBatch(tenantAccs);
        } catch (Exception e) {
            if (e instanceof DuplicateKeyException) {
                throw new BusinessException(10020, "当前租户下已入驻该账户!");
            } else {
                throw e;
            }
        }
    }

    @Override
    public void addRelation(String accId, String tenantId, AccRoleEnum accRole) {
        try {
            this.save(new TenantAcc().setAccId(accId).setTenantId(tenantId).setRole(accRole.name()));
        } catch (Exception e) {
            if (e instanceof DuplicateKeyException) {
                throw new BusinessException(10020, "当前租户下已入驻该账户!");
            } else {
                throw e;
            }
        }
    }

    @Override
    public void addRelation(List<TenantAccDto> tenantAccDtoList) {
        try {
            this.saveBatch(TenantAcc.warp(tenantAccDtoList));
        } catch (Exception e) {
            if (e instanceof DuplicateKeyException) {
                throw new BusinessException(10020, "当前租户下已入驻该账户!");
            } else {
                throw e;
            }
        }
    }

    @Override
    public void removeRelation(String accId, String tenantId) {
        this.remove(
                new LambdaQueryWrapper<TenantAcc>()
                        .eq(TenantAcc::getAccId, accId)
                        .eq(TenantAcc::getTenantId, tenantId));
    }

    @Override
    public void removeRelation(String accId) {
        this.remove(
                new LambdaQueryWrapper<TenantAcc>()
                        .eq(TenantAcc::getAccId, accId));
    }
}
