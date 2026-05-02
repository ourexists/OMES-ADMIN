/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.omes.device.pojo.EquipRecordEventEndPatch;
import com.ourexists.omes.device.pojo.EquipRecordRun;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface EquipRecordRunMapper extends BaseMapper<EquipRecordRun> {

    @Update({
            "<script>",
            "UPDATE t_equip_record_run a SET end_time = v.end_time FROM (",
            "<foreach collection='items' item='it' separator=' UNION ALL '>",
            "SELECT #{it.sn} AS sn, #{it.eventId} AS event_id, CAST(#{it.endTime} AS TIMESTAMP) AS end_time",
            "</foreach>",
            ") v WHERE a.sn = v.sn AND a.event_id = v.event_id",
            "</script>"
    })
    int batchUpdateEndTimeByEventId(@Param("items") List<EquipRecordEventEndPatch> items);

    /** 某设备累计运行分钟数(state=1)，无 end_time 的段按当前时间截断 */
    @Select("SELECT COALESCE(SUM(EXTRACT(EPOCH FROM (COALESCE(end_time, NOW()) - start_time)) / 60), 0) FROM t_equip_record_run WHERE sn = #{sn} AND state = 1")
    Long sumRunMinutesBySn(@Param("sn") String sn);

    /** 某设备运行段数(state=1)，即启停周期数 */
    @Select("SELECT COUNT(*) FROM t_equip_record_run WHERE sn = #{sn} AND state = 1")
    Long countRunSegmentsBySn(@Param("sn") String sn);
}
