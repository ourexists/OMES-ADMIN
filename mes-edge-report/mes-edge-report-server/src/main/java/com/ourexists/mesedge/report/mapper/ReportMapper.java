/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.mapper;

import com.ourexists.mesedge.report.model.MatCountQuery;
import com.ourexists.mesedge.report.model.MatCountVo;
import com.ourexists.mesedge.report.model.ProductionCountQuery;
import com.ourexists.mesedge.report.model.ProductionCountVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
@Mapper
public interface ReportMapper {

    @Select("<script>" +
            "select a.lm, sum(a.LL) as ll, sum(a.sj) as sj from (" +
            "select * from lm_record " +
            "<where>" +
            "<if test='startDate!=null and endDate!=null'>" +
            " and rq between #{startDate} and #{endDate} " +
            "</if>" +
            "<if test='matName!=null and matName!=\"\"'>" +
            " and lm = #{matName} " +
            "</if>" +
            "<if test='pfName!=null and pfName!=\"\"'>" +
            " and pf = #{pfName} " +
            "</if>" +
            "<if test='bh!=null and bh!=\"\"'>" +
            " and bh = #{bh} " +
            "</if>" +
            "<if test='line!=null and line!=\"\"'>" +
            " and line = #{line} " +
            "</if>" +
            "</where>) a " +
            "group by a.lm " +
            "order by a.lm" +
            "</script>")
    List<MatCountVo> selectMatCount(MatCountQuery query);

    @Select("<script>" +
            "select a.pf, sum(a.LL) as ll, sum(a.sj) as sj from (" +
            "select * from lm_record " +
            "<where>" +
            "<if test='startDate!=null and endDate!=null'>" +
            " and rq between #{startDate} and #{endDate} " +
            "</if>" +
            "<if test='matName!=null and matName!=\"\"'>" +
            " and lm = #{matName} " +
            "</if>" +
            "<if test='pfName!=null and pfName!=\"\"'>" +
            " and pf = #{pfName} " +
            "</if>" +
            "<if test='bh!=null and bh!=\"\"'>" +
            " and bh = #{bh} " +
            "</if>" +
            "<if test='line!=null and line!=\"\"'>" +
            " and line = #{line} " +
            "</if>" +
            "</where>) a " +
            "group by a.pf " +
            "order by a.pf" +
            "</script>")
    List<ProductionCountVo> selectPFCount(ProductionCountQuery query);
}
