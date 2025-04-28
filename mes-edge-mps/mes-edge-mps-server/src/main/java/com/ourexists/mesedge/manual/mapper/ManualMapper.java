/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.manual.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.mesedge.manual.pojo.Manual;
import com.ourexists.mesedge.mps.model.QAPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
@Mapper
public interface ManualMapper extends BaseMapper<Manual> {

    String FIELD = "a.id, a.mps_id as mpsId, a.mps_tf_id as mpsTfId, a.msg, a.result, a.pass, " +
            "a.created_by as createdBy, a.updated_by as updatedBy, a.updated_time as updatedTime, a.created_time as createdTime," +
            "a.created_id as createdId, a.updated_id as updatedId";

    @Select("<script>" +
            "select count(*) from t_qa a left join t_mps b on a.mps_id = b.id " +
            "where 1=1" +
            "<if test=\"moCode!=null and moCode!=''\">" +
            " and b.mo_code=#{moCode} " +
            "</if>" +
            "</script>")
    Long countPage(QAPageQuery dto);

    @Select("<script>" +
            "select " + FIELD + " from t_qa a left join t_mps b on a.mps_id = b.id " +
            "where 1=1" +
            "<if test=\"moCode!=null and moCode!=''\">" +
            " and b.mo_code=#{moCode} " +
            "</if>" +
            "</script>"
    )
    List<Manual> page(QAPageQuery dto);
}
