/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.depart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.core.constants.CommonConstant;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.ucenter.depart.mapper.DepartMapper;
import com.ourexists.mesedge.ucenter.depart.mapper.DepartUsersMapper;
import com.ourexists.mesedge.ucenter.depart.pojo.Depart;
import com.ourexists.mesedge.ucenter.depart.pojo.DepartUsers;
import com.ourexists.mesedge.ucenter.depart.service.DepartUsersService;
import com.ourexists.mesedge.ucenter.tenant.mapper.TenantAccMapper;
import com.ourexists.mesedge.ucenter.tenant.pojo.TenantAcc;
import org.springframework.beans.factory.annotation.Autowired;
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
public class DepartUsersServiceImpl extends AbstractMyBatisPlusService<DepartUsersMapper, DepartUsers>
        implements DepartUsersService {

    @Autowired
    private DepartMapper departMapper;

    @Autowired
    private TenantAccMapper tenantAccMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUserToUserGroup(String departId, List<String> accIds) {
        Depart userGroup = this.departMapper.selectById(departId);
        if (userGroup == null) {
            throw new BusinessException("选择的部门不存在!");
        }
        if (CollectionUtils.isEmpty(accIds)) {
            return;
        }
        List<TenantAcc> tenantAccList = tenantAccMapper.selectList(
                new LambdaQueryWrapper<TenantAcc>()
                        .eq(TenantAcc::getTenantId, userGroup.getTenantId())
                        .in(TenantAcc::getAccId, accIds)
        );
        if (CollectionUtils.isEmpty(tenantAccList)) {
            return;
        }
        List<DepartUsers> departUsersList = new ArrayList<>();
        for (TenantAcc tenantAcc : tenantAccList) {
            departUsersList.add(new DepartUsers().setAccId(tenantAcc.getAccId()).setDepartId(departId));
        }
        this.removeRelationByDepart(departId);
        this.saveBatch(departUsersList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserGroupsToUsers(String accId, List<String> userGroupIds) {
        if (CollectionUtils.isEmpty(userGroupIds)) {
            return;
        }
        //保证用户组归属对应租户
        List<Depart> departs = departMapper.selectBatchIds(userGroupIds);
        if (CollectionUtils.isEmpty(departs)) {
            return;
        }
        List<DepartUsers> groupUsersList = new ArrayList<>();
        for (Depart depart : departs) {
            groupUsersList.add(new DepartUsers().setAccId(accId).setDepartId(depart.getId()));
        }
        this.removeRelationByAcc(accId);
        this.saveBatch(groupUsersList);
    }

    @Override
    public void removeRelation(String accId, String userGroupId) {
        this.remove(
                new LambdaQueryWrapper<DepartUsers>()
                        .eq(DepartUsers::getAccId, accId)
                        .eq(DepartUsers::getDepartId, userGroupId));
    }

    @Override
    public void removeRelationByAcc(String accId) {
        if (UserContext.getTenant() != null && !CommonConstant.SYSTEM_TENANT.equals(UserContext.getTenant().getTenantId())) {
            this.baseMapper.removeRelationByAcc(accId, UserContext.getTenant().getTenantId());
        } else {
            this.remove(new LambdaQueryWrapper<DepartUsers>().eq(DepartUsers::getAccId, accId));
        }
    }


    @Override
    public void removeRelationByDepart(String departId) {
        if (UserContext.getTenant() != null && !CommonConstant.SYSTEM_TENANT.equals(UserContext.getTenant().getTenantId())) {
            this.baseMapper.removeRelationByDepart(departId, UserContext.getTenant().getTenantId());
        } else {
            this.remove(new LambdaQueryWrapper<DepartUsers>().eq(DepartUsers::getDepartId, departId));
        }
    }
}
