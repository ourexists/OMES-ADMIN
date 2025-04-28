/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.mesedge.mps.pojo.MPS;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
@Mapper
public interface MPSMapper extends BaseMapper<MPS> {

    @Select("select max(priority) from t_mps where status=#{status}")
    BigDecimal getMaxPriority(Integer status);

    @Select("select min(priority) from t_mps where status=#{status}")
    BigDecimal getMinPriority(Integer status);
}
