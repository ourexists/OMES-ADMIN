/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.core.constants.CommonConstant;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.utils.tree.TreeUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.ucenter.permission.PermissionDto;
import com.ourexists.mesedge.ucenter.permission.PermissionModifyDto;
import com.ourexists.mesedge.ucenter.permission.PermissionTreeNode;
import com.ourexists.mesedge.ucenter.permission.mapper.PermissionMapper;
import com.ourexists.mesedge.ucenter.permission.pojo.Permission;
import com.ourexists.mesedge.ucenter.permission.pojo.PermissionApi;
import com.ourexists.mesedge.ucenter.permission.service.PermissionApiService;
import com.ourexists.mesedge.ucenter.permission.service.PermissionService;
import com.ourexists.mesedge.ucenter.role.pojo.Role;
import com.ourexists.mesedge.ucenter.role.service.RoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author pengcheng
 * @date 2022/4/2 16:20
 * @since 1.0.0
 */
@Service
public class PermissionServiceImpl extends AbstractMyBatisPlusService<PermissionMapper, Permission> implements PermissionService {

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionApiService permissionApiService;

    @Override
    public void add(PermissionDto dto) {
        Permission permission = Permission.warp(dto);
        Permission other = this.getOne(
                new LambdaQueryWrapper<Permission>()
                        .eq(Permission::getCode, permission.getCode())
                        .eq(Permission::getPlatform, permission.getPlatform())
        );
        if (other != null) {
            throw new BusinessException("权限编号重复!");
        }
        if (!StringUtils.isEmpty(permission.getPcode())) {
            Permission pPermission = this.getOne(
                    new LambdaQueryWrapper<Permission>()
                            .eq(Permission::getCode, permission.getPcode())
            );
            if (pPermission == null) {
                throw new BusinessException("选择的上级权限不存在!");
            }
            String ppcode = pPermission.getPpcode();
            if (StringUtils.isEmpty(ppcode)) {
                ppcode = TreeUtil.PPID_SPLIT;
            }
            permission.setPpcode(ppcode + permission.getPcode() + TreeUtil.PPID_SPLIT);
            permission.setPlatform(dto.getPlatform());
        } else {
            if (StringUtils.isEmpty(dto.getPlatform())) {
                throw new BusinessException("请选择权限归属平台!");
            }
        }
        this.save(permission);
    }

    @Override
    public void modify(PermissionModifyDto modifyDto) {
        Permission permission = this.getById(modifyDto.getId());
        if (permission == null) {
            throw new BusinessException("选择的权限不存在!");
        }
        BeanUtils.copyProperties(modifyDto, permission);
        this.updateById(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeById(String id) {
        List<Permission> children = this.baseMapper.selectChildernById(id);
        if (!CollectionUtils.isEmpty(children)) {
            throw new BusinessException("该用户组存在下级用户组,无法删除!");
        }
        super.removeById(id);
        this.permissionApiService.clearApiPermission(id, true);
    }

    @Override
    public PermissionTreeNode selectPermissionTree(Permission parent) {
        if (parent == null) {
            throw new BusinessException("该上级权限不存在");
        }
        //查询出所有下级节点
        List<Permission> permissions = this.list(
                new LambdaQueryWrapper<Permission>()
                        .like(Permission::getPpcode, TreeUtil.PPID_SPLIT + parent.getCode() + TreeUtil.PPID_SPLIT));
        List<PermissionTreeNode> permissionTreeNodes = Permission.covert(permissions);
        PermissionTreeNode pNode = Permission.covert(parent);
        TreeUtil.mountChildrenNode(pNode, permissionTreeNodes);
        return pNode;
    }

    @Override
    public List<Permission> selectPermissionWhichTenantHold(String tenantId) {
        return selectPermissionWhichTenantHold(tenantId, null);
    }

    @Override
    public List<Permission> selectPermissionWhichTenantHold(String tenantId, String platform) {
        List<Permission> r;
        if (tenantId.equals(CommonConstant.SYSTEM_TENANT)) {
            //系统租户查询所有
            r = this.list(
                    new LambdaQueryWrapper<Permission>()
                            .eq(!StringUtils.isEmpty(platform), Permission::getPlatform, platform)
                            .orderByAsc(Permission::getSortNo)
                            .orderByAsc(Permission::getId));
        } else {
            r = this.baseMapper.selectPermissionWhichTenantHold(tenantId, platform);
        }
        return r;
    }


    @Override
    public List<Permission> selectPermissionWhichRoleHold(String roleId) {
        return this.baseMapper.selectPermissionWhichRoleHold(roleId);
    }

    @Override
    public List<Permission> selectPermissionWhichPlatformHold(String platform) {
        return this.list(
                new LambdaQueryWrapper<Permission>()
                        .eq(Permission::getPlatform, platform)
                        .orderByAsc(Permission::getSortNo)
                        .orderByAsc(Permission::getId));
    }

    @Override
    public List<Permission> selectPermissionWhichAccHold(String accId) {
        return selectPermissionWhichAccHold(accId, null);
    }

    @Override
    public List<Permission> selectPermissionWhichAccHold(String accId, String platform) {
        List<Role> roles = roleService.selectRoleWhichAccHold(accId, true);
        if (CollectionUtils.isEmpty(roles)) {
            return null;
        }
        List<String> roleIds = roles.stream().map(Role::getId).collect(Collectors.toList());
        return this.baseMapper.selectPermissionWhichRolesHold(roleIds, null, platform);
    }

    @Override
    public List<Permission> selectPermissionWhichRoleHold(String roleId, String platform) {
        return this.baseMapper.selectPermissionWhichRolesHold(Collections.singletonList(roleId), null, platform);
    }


    @Override
    public Map<String, List<PermissionApi>> selectAccPermissionApiGroupByTenant(String accId) {
        List<Role> roles = roleService.selectRoleWhichAccHold(accId, true);
        if (CollectionUtils.isEmpty(roles)) {
            return null;
        }
        Map<String, List<String>> roleidmap = new HashMap<>(16);
        for (Role role : roles) {
            List<String> roleids = roleidmap.get(role.getTenantId());
            if (roleids == null) {
                roleids = new ArrayList<>();
            }
            roleids.add(role.getId());
            roleidmap.put(role.getTenantId(), roleids);
        }
        Map<String, List<PermissionApi>> res = new HashMap<>(roleidmap.size());
        for (Map.Entry<String, List<String>> entry : roleidmap.entrySet()) {
            List<Permission> permissions = this.baseMapper.selectPermissionWhichRolesHold(entry.getValue(), null, null);
            List<String> permissionIds = permissions.stream().map(Permission::getId).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(permissionIds)) {
                res.put(entry.getKey(),
                        this.permissionApiService.list(
                                new LambdaQueryWrapper<PermissionApi>()
                                        .in(PermissionApi::getPermissionId, permissionIds)));
            }
        }
        return res;
    }
}
