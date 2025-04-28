/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.depart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.mesedge.ucenter.depart.pojo.Depart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
@Mapper
public interface DepartMapper extends BaseMapper<Depart> {

    String FIELD = "a.id, a.revision, a.created_by as createdBy, a.created_id as createdId, a.created_time as createdTime," +
            "a.updated_by as updatedBy, a.updated_id as updatedId,a.updated_time as updatedTime,a.tenant_id as tenantId," +
            "a.name, a.code, a.pcode, a.ppcode";

    @Select("select " + FIELD + " from t_ucenter_depart a " +
            "where pcode = (select code from t_ucenter_depart where id=#{id})")
    List<Depart> selectChildernById(String id);


    @Select("select " + FIELD + " from t_ucenter_depart a " +
            "left join r_ucenter_depart_users b " +
            "on a.id=b.depart_id " +
            "where b.acc_id=#{accId} " +
            " order by a.id asc ")
    List<Depart> selectDepartWhichAccBelong(String accId);
}
