/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.depart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.mesedge.ucenter.depart.pojo.DepartUsers;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
@Mapper
public interface DepartUsersMapper extends BaseMapper<DepartUsers> {

    @Delete("delete a from r_ucenter_depart_users a, t_ucenter_depart b " +
            "where b.id=a.depart_id " +
            "and a.acc_id=#{accId} " +
            "and b.tenant_id=#{tenantId}")
    Integer removeRelationByAcc(String accId, String tenantId);

    @Delete("delete a from r_ucenter_depart_users a, t_ucenter_depart b " +
            "where b.id=a.depart_id " +
            "and a.depart_id=#{departId} " +
            "and b.tenant_id=#{tenantId}")
    Integer removeRelationByDepart(String departId, String tenantId);
}
