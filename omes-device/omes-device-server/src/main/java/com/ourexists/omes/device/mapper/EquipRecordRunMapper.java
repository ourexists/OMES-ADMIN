/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.omes.device.pojo.EquipRecordRun;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EquipRecordRunMapper extends BaseMapper<EquipRecordRun> {

    /** 某设备累计运行分钟数(state=1)，无 end_time 的段按当前时间截断 */
    @Select("SELECT COALESCE(SUM(EXTRACT(EPOCH FROM (COALESCE(end_time, NOW()) - start_time)) / 60), 0) FROM t_equip_record_run WHERE sn = #{sn} AND state = 1")
    Long sumRunMinutesBySn(@Param("sn") String sn);

    /** 某设备运行段数(state=1)，即启停周期数 */
    @Select("SELECT COUNT(*) FROM t_equip_record_run WHERE sn = #{sn} AND state = 1")
    Long countRunSegmentsBySn(@Param("sn") String sn);
}
