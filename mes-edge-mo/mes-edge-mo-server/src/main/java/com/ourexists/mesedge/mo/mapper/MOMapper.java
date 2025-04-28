/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.mesedge.mo.pojo.MO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
@Mapper
public interface MOMapper extends BaseMapper<MO> {
    @Update("update t_mo " +
            "set status= case " +
            "when #{surplus}=0 then 2 " +
            "when num = #{surplus} then 0 " +
            "else 1" +
            "end, " +
            "surplus=#{surplus} " +
            "where self_code=#{code}")
    void updateSurplus(String code, int surplus);
}
