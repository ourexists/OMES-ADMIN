/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.role.service;


import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.ucenter.role.pojo.AccRole;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface AccRoleService extends IMyBatisPlusService<AccRole> {

    /**
     * 建立关联
     *
     * @param accId
     * @param roleIds
     */
    void establishRelation(String accId, List<String> roleIds);

    /**
     * 移除关联
     *
     * @param accId
     */
    void removeRelationByAcc(String accId);

    /**
     * 移除关联
     *
     * @param roleId
     */
    void removeRelationByRole(String roleId);

    /**
     * 移除关联
     *
     * @param roleIds
     */
    void removeRelationByRole(List<String> roleIds);
}
