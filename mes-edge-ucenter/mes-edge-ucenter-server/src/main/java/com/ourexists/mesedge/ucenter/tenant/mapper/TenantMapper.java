/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.mesedge.ucenter.tenant.TenantUVo;
import com.ourexists.mesedge.ucenter.tenant.pojo.Tenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {

    String FIELD = "a.id, a.revision, a.created_by as createdBy, a.created_id as createdId, a.created_time as createdTime," +
            "a.updated_by as updatedBy, a.updated_id as updatedId,a.updated_time as updatedTime," +
            "a.tenant_name as tenantName, a.tenant_code as tenantCode, a.pcode, a.status, a.settled_time as settledTime," +
            "a.expire_time as expireTime, a.tenant_address as tenantAddress, a.tenant_contacts as tenantContacts, " +
            "a.tenant_phone as tenantPhone, a.tenant_mail as tenantMail, a.province_code as provinceCode, a.city_code as cityCode," +
            "a.county_code as countyCode, a.street_code as streetCode, a.area_fullname as areaFullname, a.tenant_coo as tenantCoo," +
            "a.management as management, a.manage_num as manageNum, a.logo as logo ";

    @Select("select " + FIELD + " from r_ucenter_tenant_acc b left join p_ucenter_tenant a " +
            "on a.tenant_code = b.tenant_id " +
            "and b.acc_id = #{accId} " +
            "where a.del_flag = 0 " +
            "and a.status = 0 " +
            "and a.settled_time <= #{date} " +
            "and a.expire_time >= #{date} ")
    List<Tenant> tenantToWhichTheAccBelong(String accId, Date date);

    @Select("select " + FIELD + " from r_ucenter_tenant_acc b left join p_ucenter_tenant a " +
            "on a.tenant_code = b.tenant_id  " +
            "where b.acc_id = #{accId} and a.settled_time <= #{date} and a.expire_time >= #{date} and a.del_flag = 0")
    List<Tenant> availableTenantToWhichTheAccBelong(String accId, Date date);


    @Select("select " + FIELD + ", b.acc_id as userId, b.role from r_ucenter_tenant_acc b left join  p_ucenter_tenant a " +
            "on a.tenant_code = b.tenant_id " +
            "where b.acc_id = #{accId} and a.settled_time <= #{date} and a.expire_time >= #{date} and a.del_flag = 0")
    List<TenantUVo> availableTenantRoleWhichTheAccBelong(String accId, Date date);

    @Select("select " + FIELD + ", b.acc_id as userId, b.role from r_ucenter_tenant_acc b left join p_ucenter_tenant a " +
            "on a.tenant_code = b.tenant_id " +
            "where b.acc_id in " +
            "<foreach item='accId' collection='accIds' separator=',' open='(' close=')'>" +
            "#{accId}" +
            "</foreach>" +
            "and  a.settled_time <= #{date} and a.expire_time >= #{date} and a.del_flag = 0")
    List<TenantUVo> availableTenantToWhichTheAccsBelong(List<String> accIds, Date date);

}
