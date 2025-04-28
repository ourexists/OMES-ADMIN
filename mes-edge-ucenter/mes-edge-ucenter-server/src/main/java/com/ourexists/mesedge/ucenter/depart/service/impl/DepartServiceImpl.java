/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.depart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.utils.tree.TreeUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.ucenter.depart.DepartDto;
import com.ourexists.mesedge.ucenter.depart.DepartModifyDto;
import com.ourexists.mesedge.ucenter.depart.DepartPageQuery;
import com.ourexists.mesedge.ucenter.depart.DepartTreeNode;
import com.ourexists.mesedge.ucenter.depart.mapper.DepartMapper;
import com.ourexists.mesedge.ucenter.depart.pojo.Depart;
import com.ourexists.mesedge.ucenter.depart.pojo.DepartUsers;
import com.ourexists.mesedge.ucenter.depart.service.DepartService;
import com.ourexists.mesedge.ucenter.depart.service.DepartUsersService;
import com.ourexists.mesedge.ucenter.role.service.DepartRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:20
 * @since 1.0.0
 */
@Service
public class DepartServiceImpl extends AbstractMyBatisPlusService<DepartMapper, Depart> implements DepartService {

    @Autowired
    private DepartUsersService departUsersService;

    @Autowired
    private DepartRoleService departRoleService;

    @Override
    public Page<Depart> selectByPage(DepartPageQuery pageQuery) {
        LambdaQueryWrapper<Depart> wrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(pageQuery.getCode())) {
            wrapper.eq(Depart::getCode, pageQuery.getCode());
        }
        if (!StringUtils.isEmpty(pageQuery.getName())) {
            wrapper.like(Depart::getName, pageQuery.getName());
        }
        wrapper.orderByDesc(Depart::getId);
        return this.page(new Page<>(pageQuery.getPage(), pageQuery.getPageSize()), wrapper);
    }

    @Override
    public void add(DepartDto userGroupDto) {
        Depart userGroup = Depart.warp(userGroupDto);
        Depart other = this.getOne(
                new LambdaQueryWrapper<Depart>()
                        .eq(Depart::getCode, userGroup.getCode()));
        if (other != null) {
            throw new BusinessException("部门编号重复!");
        }
        if (userGroup.getPcode() != null) {
            Depart pDepart = this.getOne(
                    new LambdaQueryWrapper<Depart>()
                            .eq(Depart::getCode, userGroup.getPcode()));
            if (pDepart == null) {
                throw new BusinessException("选择的上级部门不存在!");
            }
            String ppcode = pDepart.getPpcode();
            if (StringUtils.isEmpty(ppcode)) {
                ppcode = TreeUtil.PPID_SPLIT;
            }
            userGroup.setPpcode(ppcode + userGroup.getPcode() + TreeUtil.PPID_SPLIT);
        }
        this.save(userGroup);
    }

    @Override
    public void modify(DepartModifyDto modifyDto) {
        Depart userGroup = this.getById(modifyDto.getId());
        if (userGroup == null) {
            return;
        }
        userGroup.setName(modifyDto.getName());
        this.updateById(userGroup);
    }

    @Override
    public void removeById(String id) {
        List<Depart> children = this.baseMapper.selectChildernById(id);
        if (!CollectionUtils.isEmpty(children)) {
            throw new BusinessException("该部门存在下级部门,无法删除!");
        }
        List<DepartUsers> userRelates = departUsersService.list(
                new LambdaQueryWrapper<DepartUsers>().eq(DepartUsers::getDepartId, id));
        if (!CollectionUtils.isEmpty(userRelates)) {
            throw new BusinessException("该部门存在用户,无法删除!");
        }
        this.baseMapper.deleteById(id);
        departRoleService.removeRelationByDepart(id);
    }

    @Override
    public List<Depart> selectDepartWhichAccBelong(String accId) {
        return this.baseMapper.selectDepartWhichAccBelong(accId);
    }

    @Override
    public DepartTreeNode selectDepartTreeById(String pid) {
        return selectDepartTree(this.getById(pid));
    }

    @Override
    public DepartTreeNode selectDepartTreeByCode(String code) {
        return selectDepartTree(this.getOne(new LambdaQueryWrapper<Depart>().eq(Depart::getCode, code)));
    }

    @Override
    public DepartTreeNode selectDepartTree(Depart parent) {
        if (parent == null) {
            throw new BusinessException("该上级部门不存在");
        }
        //查询出所有下级节点
        List<Depart> departs = this.list(
                new LambdaQueryWrapper<Depart>()
                        .like(Depart::getPcode, TreeUtil.PPID_SPLIT + parent.getCode() + TreeUtil.PPID_SPLIT)
                        .orderByAsc(Depart::getId)
        );
        List<DepartTreeNode> departTreeNodes = Depart.covert(departs);
        DepartTreeNode pNode = Depart.covert(parent);
        TreeUtil.mountChildrenNode(pNode, departTreeNodes);
        return pNode;
    }

    @Override
    public List<DepartTreeNode> selectAllDepartTree() {
        List<DepartTreeNode> result = new ArrayList<>();
        List<Depart> rootDeparts = this.list();
        if (CollectionUtils.isEmpty(rootDeparts)) {
            return result;
        }
        result = Depart.covert(rootDeparts);
        return TreeUtil.foldRootTree(result);
    }
}
