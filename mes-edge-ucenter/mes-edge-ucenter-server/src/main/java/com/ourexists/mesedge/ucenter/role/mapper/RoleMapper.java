/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.mesedge.ucenter.role.pojo.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    String FIELD = "a.id, a.revision, a.created_by as createdBy, a.created_id as createdId, a.created_time as createdTime," +
            "a.updated_by as updatedBy, a.updated_id as updatedId,a.updated_time as updatedTime, a.tenant_id as tenantId, " +
            "a.name, a.code, a.description ";

    @Select("select " + FIELD + " from t_ucenter_role a " +
            "left join r_ucenter_acc_role b " +
            "on a.id=b.role_id " +
            "where b.acc_id=#{accId} ")
    List<Role> selectRoleWhichAccHold(String accId);

    @Select("select " + FIELD + " from t_ucenter_role a " +
            "left join r_ucenter_depart_role b " +
            "on a.id=b.role_id " +
            "where b.depart_id=#{departId} ")
    List<Role> selectRoleWhichDepartHold(String departId);

    @Select("<script>" +
            "select " + FIELD + " from t_ucenter_role a " +
            "left join r_ucenter_depart_role b " +
            "on a.id=b.role_id " +
            "where b.depart_id in " +
            "<foreach item=\"departId\" index=\"index\" collection=\"departIds\" open=\"(\" separator=\",\" close=\")\">" +
            "  #{departId}" +
            "</foreach>" +
            "</script>")
    List<Role> selectRoleWhichDepartsHold(List<String> departIds);
}
