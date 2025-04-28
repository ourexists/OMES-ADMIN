/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.depart.service;


import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.ucenter.depart.pojo.DepartUsers;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface DepartUsersService extends IMyBatisPlusService<DepartUsers> {

    /**
     * 将用户添加进用户组
     */
    void addUserToUserGroup(String userGroupId, List<String> accIds);

    /**
     * 为账户分配用户组
     *
     * @param userGroupIds 用户组ids
     * @param accId        账户id
     */
    void assignUserGroupsToUsers(String accId, List<String> userGroupIds);

    /**
     * 解除关联
     *
     * @param accId       账户id
     * @param userGroupId 用户组id
     */
    void removeRelation(String accId, String userGroupId);

    /**
     * 解除关联
     *
     * @param accId 账户id
     */
    void removeRelationByAcc(String accId);


    /**
     * 解除关联
     *
     * @param departId 部门id
     */
    void removeRelationByDepart(String departId);
}
