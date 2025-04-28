/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.role.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.ucenter.role.RoleDto;
import com.ourexists.mesedge.ucenter.role.RolePageQuery;
import com.ourexists.mesedge.ucenter.role.pojo.Role;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface RoleService extends IMyBatisPlusService<Role> {

    /**
     * 分页查询
     *
     * @param pageQuery
     * @return
     */
    Page<Role> selectByPage(RolePageQuery pageQuery);

    /**
     * 新增用户组
     *
     * @param dto
     */
    void addOrUpdate(RoleDto dto);

    /**
     * 移除
     *
     * @param ids
     */
    void remove(List<String> ids);

    /**
     * 用户持有角色
     *
     * @param accId             账户id
     * @param containDepartRole 是否包含用户所在部门的角色
     * @return
     */
    List<Role> selectRoleWhichAccHold(String accId, boolean containDepartRole);

    /**
     * 部门持有角色
     *
     * @param departId 部门id
     * @return
     */
    List<Role> selectRoleWhichDepartHold(String departId);
}
