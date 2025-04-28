/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.role.service;


import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.ucenter.role.pojo.DepartRole;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface DepartRoleService extends IMyBatisPlusService<DepartRole> {

    /**
     * 建立关联
     * @param departId
     * @param roleIds
     */
    void establishRelation(String departId, List<String> roleIds);

    /**
     * 移除关联
     * @param departId
     */
    void removeRelationByDepart(String departId);

    /**
     * 移除关联
     * @param roleId
     */
    void removeRelationByRole(String roleId);


    /**
     * 移除关联
     * @param roleIds
     */
    void removeRelationByRole(List<String> roleIds);
}
