/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.role.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.ucenter.depart.pojo.Depart;
import com.ourexists.mesedge.ucenter.depart.service.DepartService;
import com.ourexists.mesedge.ucenter.role.RoleDto;
import com.ourexists.mesedge.ucenter.role.RolePageQuery;
import com.ourexists.mesedge.ucenter.role.mapper.RoleMapper;
import com.ourexists.mesedge.ucenter.role.pojo.Role;
import com.ourexists.mesedge.ucenter.role.service.AccRoleService;
import com.ourexists.mesedge.ucenter.role.service.DepartRoleService;
import com.ourexists.mesedge.ucenter.role.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author pengcheng
 * @date 2022/4/2 16:20
 * @since 1.0.0
 */
@Service
public class RoleServiceImpl extends AbstractMyBatisPlusService<RoleMapper, Role> implements RoleService {

    @Autowired
    private AccRoleService accRoleService;

    @Autowired
    private DepartRoleService departRoleService;

    @Autowired
    private DepartService departService;

    @Override
    public Page<Role> selectByPage(RolePageQuery pageQuery) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<Role>()
//                .eq(pageQuery.getType() != null, Role::getType, pageQuery.getType())
                .eq(StringUtils.hasText(pageQuery.getCode()), Role::getCode, pageQuery.getCode())
                .eq(StringUtils.hasText(pageQuery.getTenantId()), Role::getTenantId, pageQuery.getTenantId())
                .like(StringUtils.hasText(pageQuery.getName()), Role::getName, pageQuery.getName())
                .in(CollectionUtil.isNotBlank(pageQuery.getIds()), Role::getId, pageQuery.getIds())
                .orderByDesc(Role::getId);
        return this.page(new Page<>(pageQuery.getPage(), pageQuery.getPageSize()), wrapper);
    }

    @Override
    public void addOrUpdate(RoleDto dto) {
        Role other = this.getOne(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getCode, dto.getCode())
                        .eq(Role::getType, dto.getType()));
        if (other != null) {
            if (StringUtils.isEmpty(dto.getId())) {
                throw new BusinessException("角色编号重复!");
            }
            if (!other.getId().equals(dto.getId())) {
                throw new BusinessException("角色编号重复!");
            }
        }
        this.saveOrUpdate(Role.warp(dto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(List<String> ids) {
        this.removeBatchByIds(ids);
        accRoleService.removeRelationByRole(ids);
        departRoleService.removeRelationByRole(ids);
    }

    @Override
    public List<Role> selectRoleWhichAccHold(String accId, boolean containDepartRole) {
        List<Role> roles = this.baseMapper.selectRoleWhichAccHold(accId);
        if (containDepartRole) {
            List<Depart> departs = departService.selectDepartWhichAccBelong(accId);
            if (CollectionUtils.isEmpty(departs)) {
                return roles;
            }
            List<String> departIds = departs.stream().map(Depart::getId).collect(Collectors.toList());
            List<Role> departRoles = this.baseMapper.selectRoleWhichDepartsHold(departIds);
            roles.addAll(departRoles);
        }
        return roles;
    }

    @Override
    public List<Role> selectRoleWhichDepartHold(String departId) {
        return this.baseMapper.selectRoleWhichDepartHold(departId);
    }
}
