/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.mesedge.ucenter.account.AccPageQuery;
import com.ourexists.mesedge.ucenter.account.pojo.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
@Mapper
public interface AccountMapper extends BaseMapper<Account> {

    String FIELD = "a.id, a.revision, a.created_by as createdBy, a.created_id as createdId, a.created_time as createdTime," +
            "a.updated_by as updatedBy, a.updated_id as updatedId,a.updated_time as updatedTime," +
            "a.acc_name as accName, a.password, a.nick_name as nickName, a.user_name as userName, a.status, " +
            "a.settled_time as settledTime, a.expire_time as expireTime, a.id_card as idCard," +
            "a.birth_day as birthDay, a.sex, a.mobile, a.email, a.init, a.platform ";

    @Select("<script>" +
            "select count(1) from (" +
            "select * from p_ucenter_acc a " +
            "left join r_ucenter_tenant_acc b " +
            "on a.id = b.acc_id " +
            "<where>" +
            "<if test= \"platform!=null and platform!=''\">" +
            " and a.platform = #{platform}" +
            "</if>" +
            "<if test= \"tenantId!=null and tenantId!=''\">" +
            " and b.tenant_id = #{tenantId}" +
            "</if>" +
            "<if test=\"accName!=null and accName!=''\">" +
            " and a.acc_name like CONCAT('%', #{accName}, '%')" +
            "</if>" +
            "<if test=\"nickName!=null and nickName!=''\">" +
            " and a.nick_name like CONCAT('%', #{nickName}, '%')" +
            "</if>" +
            "<if test=\"status!=null\">" +
            " and a.status = #{status}" +
            "</if>" +
            "<if test=\"expireStartTime!=null and expireEndTime!=null\">" +
            " and a.expire_time &gt;= #{expireStartTime} and a.expire_time &lt;= #{expireEndTime}" +
            "</if>" +
            "<if test=\"mobile!=null and mobile!=''\">" +
            " and a.mobile = #{mobile}" +
            "</if>" +
            "<if test= \"perfection!=null\">" +
            " and a.perfection = #{perfection}" +
            "</if>" +
            "<if test= \"accRole!=null  and accRole!=''\">" +
            " and b.role = #{accRole}" +
            "</if>" +
            "<if test= \"pTenant!=null and pTenant!=''\">" +
            " and b.tenant_id like CONCAT(#{pTenant}, '%')" +
            "</if>" +
            "<if test= \"ids!=null\">" +
            " and a.id in " +
            "<foreach item=\"id\" collection=\"ids\" open=\"(\" separator=\",\" close=\")\">" +
            " #{id}" +
            "</foreach>" +
            "</if>" +
            "</where>" +
            "group by a.id ) c" +
            "</script>")
    int pageCount(AccPageQuery pageQuery);


    @Select("<script>" +
            "select " + FIELD + " from p_ucenter_acc a " +
            "left join r_ucenter_tenant_acc b " +
            "on a.id = b.acc_id " +
            "<where>" +
            "<if test= \"platform!=null and platform!=''\">" +
            " and a.platform = #{platform}" +
            "</if>" +
            "<if test= \"tenantId!=null and tenantId!=''\">" +
            " and b.tenant_id = #{tenantId}" +
            "</if>" +
            "<if test=\"accName!=null and accName!=''\">" +
            " and a.acc_name like CONCAT('%', #{accName}, '%')" +
            "</if>" +
            "<if test=\"nickName!=null and nickName!=''\">" +
            " and a.nick_name like CONCAT('%', #{nickName}, '%')" +
            "</if>" +
            "<if test=\"status!=null\">" +
            " and a.status = #{status}" +
            "</if>" +
            "<if test=\"expireStartTime!=null and expireEndTime!=null\">" +
            " and a.expire_time &gt;= #{expireStartTime} and a.expire_time &lt;= #{expireEndTime}" +
            "</if>" +
            "<if test=\"mobile!=null and mobile!=''\">" +
            " and a.mobile = #{mobile}" +
            "</if>" +
            "<if test= \"perfection!=null\">" +
            " and a.perfection = #{perfection}" +
            "</if>" +
            "<if test= \"accRole!=null and accRole!=''\">" +
            " and b.role = #{accRole}" +
            "</if>" +
            "<if test= \"pTenant!=null and pTenant!=''\">" +
            " and b.tenant_id like CONCAT(#{pTenant}, '%')" +
            "</if>" +
            "<if test= \"ids!=null\">" +
            " and a.id in " +
            "<foreach item=\"id\" collection=\"ids\" open=\"(\" separator=\",\" close=\")\">" +
            " #{id}" +
            "</foreach>" +
            "</if>" +
            "</where>" +
            "group by a.id " +
            "order by id desc " +
            "limit #{limitPage}, #{pageSize} " +
            "</script>")
    List<Account> page(AccPageQuery pageQuery);

    @Select("select " + FIELD + " from p_ucenter_acc a left join r_ucenter_depart_users b " +
            "on a.id = b.acc_id where b.depart_id=#{departId}")
    List<Account> selectByDepart(String departId);
}
