/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.mesedge.ucenter.permission.pojo.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    String FIELD = "a.id, a.revision, a.created_by as createdBy, a.created_id as createdId, a.created_time as createdTime," +
            "a.updated_by as updatedBy, a.updated_id as updatedId,a.updated_time as updatedTime," +
            "a.name, a.code, a.pcode, a.ppcode, a.strategy, a.icon, a.component, a.url, a.sort_no, a.type, a.keep_alive," +
            "a.description, a.internal_or_external";

    @Select("select " + FIELD + " from p_ucenter_permission a" +
            " where pcode = (select code from p_ucenter_permission where id=#{id})" +
            " order by a.sort_no asc, a.id asc")
    List<Permission> selectChildernById(String id);

    @Select("<script>" +
            "select " + FIELD + " from p_ucenter_permission a left join r_ucenter_tenant_permission b " +
            "on a.id=b.permission_id " +
            "where b.tenant_id = #{tenantId} " +
            "<if test=\"platform!=null and platform!=''\">" +
            "and a.platform = #{platform} " +
            "</if>" +
            " order by a.sort_no asc, a.id asc" +
            "</script>")
    List<Permission> selectPermissionWhichTenantHold(String tenantId, String platform);

    @Select("select " + FIELD + " from p_ucenter_permission a left join r_ucenter_role_permission b " +
            "on a.id=b.permission_id " +
            "where b.role_id = #{roleId}" +
            " order by a.sort_no asc, a.id asc")
    List<Permission> selectPermissionWhichRoleHold(String roleId);

    @Select("<script>" +
            "select " + FIELD + " from p_ucenter_permission a left join r_ucenter_role_permission b " +
            "on a.id=b.permission_id " +
            "where b.role_id in " +
            "<foreach item=\"roleId\" index=\"index\" collection=\"roleIds\" open=\"(\" separator=\",\" close=\")\">" +
            " #{roleId}" +
            "</foreach>" +
            "<if test=\"rootCode!=null and rootCode!=''\">" +
            " and a.ppcode like concat('%;', #{rootCode}, ';%') " +
            "</if>" +
            "<if  test=\"platform!=null and platform!=''\">" +
            " and a.platform = #{platform}" +
            "</if>" +
            " group by a.id " +
            " order by a.sort_no asc, a.id asc " +
            "</script>")
    List<Permission> selectPermissionWhichRolesHold(List<String> roleIds, String rootCode, String platform);
}
