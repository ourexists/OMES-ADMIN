/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.depart.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.ucenter.depart.DepartDto;
import com.ourexists.mesedge.ucenter.depart.DepartModifyDto;
import com.ourexists.mesedge.ucenter.depart.DepartPageQuery;
import com.ourexists.mesedge.ucenter.depart.DepartTreeNode;
import com.ourexists.mesedge.ucenter.depart.pojo.Depart;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface DepartService extends IMyBatisPlusService<Depart> {

    /**
     * 分页查询
     *
     * @param pageQuery
     * @return
     */
    Page<Depart> selectByPage(DepartPageQuery pageQuery);

    /**
     * 新增部门
     *
     * @param userGroupDto
     */
    void add(DepartDto userGroupDto);

    /**
     * 修改部门
     *
     * @param modifyDto
     */
    void modify(DepartModifyDto modifyDto);

    /**
     * 删除部门
     *
     * @param id 部门id
     */
    void removeById(String id);

    /**
     * 查询账户所属部门
     *
     * @param accId 账户id
     * @return
     */
    List<Depart> selectDepartWhichAccBelong(String accId);

    /**
     * 查询部门树
     *
     * @param pid 父部门id
     * @return
     */
    DepartTreeNode selectDepartTreeById(String pid);

    /**
     * 查询部门树
     *
     * @param code 父部门code
     * @return
     */
    DepartTreeNode selectDepartTreeByCode(String code);

    /**
     * 查询部门树
     *
     * @param parent 父部门
     * @return
     */
    DepartTreeNode selectDepartTree(Depart parent);

    /**
     * 查询所有的部门树
     *
     * @return
     */
    List<DepartTreeNode> selectAllDepartTree();


}
