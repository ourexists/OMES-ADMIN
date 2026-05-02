/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.omes.device.pojo.EquipRecordAlarm;
import com.ourexists.omes.device.pojo.EquipRecordEventEndPatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
@Mapper
public interface EquipRecordAlarmMapper extends BaseMapper<EquipRecordAlarm> {

    @Update({
            "<script>",
            "UPDATE t_equip_record_alarm a SET end_time = v.end_time FROM (",
            "<foreach collection='items' item='it' separator=' UNION ALL '>",
            "SELECT #{it.sn} AS sn, #{it.eventId} AS event_id, CAST(#{it.endTime} AS TIMESTAMP) AS end_time",
            "</foreach>",
            ") v WHERE a.sn = v.sn AND a.event_id = v.event_id",
            "</script>"
    })
    int batchUpdateEndTimeByEventId(@Param("items") List<EquipRecordEventEndPatch> items);
}
