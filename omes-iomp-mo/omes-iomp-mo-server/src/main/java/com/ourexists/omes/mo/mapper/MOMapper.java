/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.mo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.omes.mo.pojo.MO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
@Mapper
public interface MOMapper extends BaseMapper<MO> {
    /**
     * 回写剩余量并重算状态。已 CANCEL(4) 的订单保持 CANCEL，不被 surplus 规则覆盖。
     */
    @Update("update t_mo " +
            "set status= case " +
            "when status = 4 then 4 " +
            "when #{surplus}=0 then 2 " +
            "when num = #{surplus} then 0 " +
            "else 1" +
            "end, " +
            "surplus=#{surplus} " +
            "where self_code=#{code}")
    void updateSurplus(String code, int surplus);

    /**
     * 同步更新计划数量与剩余量。CANCEL(4)/COMPLETE(3) 保持原状态。
     */
    @Update("update t_mo " +
            "set num=#{num}, surplus=#{surplus}, " +
            "status= case " +
            "when status = 4 then 4 " +
            "when status = 3 then 3 " +
            "when #{surplus}=0 then 2 " +
            "when #{num} = #{surplus} then 0 " +
            "else 1 end " +
            "where self_code=#{code}")
    void updateNumAndSurplus(String code, int num, int surplus);
}
