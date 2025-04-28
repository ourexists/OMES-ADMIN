/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.role.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.ucenter.role.mapper.DepartRoleMapper;
import com.ourexists.mesedge.ucenter.role.pojo.DepartRole;
import com.ourexists.mesedge.ucenter.role.service.DepartRoleService;
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
public class DepartRoleServiceImpl extends AbstractMyBatisPlusService<DepartRoleMapper, DepartRole> implements DepartRoleService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void establishRelation(String departId, List<String> roleIds) {
        this.removeRelationByDepart(departId);
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }
        List<DepartRole> departRoles = new ArrayList<>();
        for (String roleId : roleIds) {
            departRoles.add(new DepartRole().setDepartId(departId).setRoleId(roleId));
        }
        this.saveBatch(departRoles);
    }

    @Override
    public void removeRelationByDepart(String departId) {
        this.remove(new LambdaQueryWrapper<DepartRole>().eq(DepartRole::getDepartId, departId));
    }

    @Override
    public void removeRelationByRole(String roleId) {
        this.remove(new LambdaQueryWrapper<DepartRole>().eq(DepartRole::getRoleId, roleId));
    }

    @Override
    public void removeRelationByRole(List<String> roleIds) {
        this.remove(new LambdaQueryWrapper<DepartRole>().in(DepartRole::getRoleId, roleIds));
    }
}
