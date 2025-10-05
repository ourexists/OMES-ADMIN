/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.mesedge.report.model.LmRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
@Mapper
public interface LmRecordMapper extends BaseMapper<LmRecord> {

    @Select("select sum(ll) as lls  from lm_record where FZ_ID= #{fzId}")
    Long selectSumll(@Param("fzId") Integer fzId);

    @Select("select sum(sj) as lls  from lm_record where FZ_ID= #{fzId}")
    Long selectSumSj(@Param("fzId") Integer fzId);
}
