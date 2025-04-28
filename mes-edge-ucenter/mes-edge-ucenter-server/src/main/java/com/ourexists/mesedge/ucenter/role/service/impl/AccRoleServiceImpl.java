/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.role.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.core.constants.CommonConstant;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.ucenter.role.mapper.AccRoleMapper;
import com.ourexists.mesedge.ucenter.role.pojo.AccRole;
import com.ourexists.mesedge.ucenter.role.service.AccRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:20
 * @since 1.0.0
 */
@Service
public class AccRoleServiceImpl extends AbstractMyBatisPlusService<AccRoleMapper, AccRole> implements AccRoleService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void establishRelation(String accId, List<String> roleIds) {
        this.removeRelationByAcc(accId);
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }
        List<AccRole> accRoles = new ArrayList<>();
        for (String roleId : roleIds) {
            accRoles.add(new AccRole().setAccId(accId).setRoleId(roleId));
        }
        this.saveBatch(accRoles);
    }

    @Override
    public void removeRelationByAcc(String accId) {
        if (UserContext.getTenant() != null && !CommonConstant.SYSTEM_TENANT.equals(UserContext.getTenant().getTenantId())) {
            this.baseMapper.removeRelationByAcc(accId, UserContext.getTenant().getTenantId());
        } else {
            this.remove(new LambdaQueryWrapper<AccRole>().eq(AccRole::getAccId, accId));
        }
    }

    @Override
    public void removeRelationByRole(String roleId) {
        if (UserContext.getTenant() != null && !CommonConstant.SYSTEM_TENANT.equals(UserContext.getTenant().getTenantId())) {
            this.baseMapper.removeRelationByRole(roleId, UserContext.getTenant().getTenantId());
        } else {
            this.remove(new LambdaQueryWrapper<AccRole>().eq(AccRole::getRoleId, roleId));
        }
    }

    @Override
    public void removeRelationByRole(List<String> roleIds) {
        this.remove(new LambdaQueryWrapper<AccRole>().in(AccRole::getRoleId, roleIds));
    }
}
