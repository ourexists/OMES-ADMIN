/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.mesedge.ucenter.role.pojo.AccRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
@Mapper
public interface AccRoleMapper extends BaseMapper<AccRole> {

    @Delete("delete a from r_ucenter_acc_role a , t_ucenter_role b " +
            "where b.id=a.role_id " +
            "and a.acc_id=#{accId} " +
            "and b.tenant_id=#{tenantId}")
    Integer removeRelationByAcc(String accId, String tenantId);

    @Delete("delete a from r_ucenter_acc_role a , t_ucenter_role b " +
            "where b.id=a.role_id " +
            "and a.role_id=#{roleId} " +
            "and b.tenant_id=#{tenantId}")
    Integer removeRelationByRole(String roleId, String tenantId);
}
