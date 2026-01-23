/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.mesedge.device.model.EquipStateSnapshotCountDto;
import com.ourexists.mesedge.device.model.EquipStateSnapshotCountQuery;
import com.ourexists.mesedge.device.pojo.EquipStateSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
@Mapper
public interface EquipStateSnapshotMapper extends BaseMapper<EquipStateSnapshot> {

    @Select("<script>" +
            "select count(a.sn) as num, " +
            "a.time " +
            "from t_equip_state_snapshot a" +
            "<where>" +
            "<if test='runState!=null'>" +
            "and a.run_state = #{runState} " +
            "</if>" +
            "<if test='alarmState!=null'>" +
            "and a.alarm_state = #{alarmState} " +
            "</if>" +
            "<if test='onlineState!=null'>" +
            "and a.online_state = #{onlineState} " +
            "</if>" +
            "<if test='startDate!=null and endDate!=null'>" +
            "and a.time between #{startDate} and #{endDate} " +
            "</if>" +
            "<if test='workshopCodes!=null and workshopCodes.size>0'>" +
            "and a.sn in (select self_code from t_equip where workshop_code in   " +
            "<foreach collection='workshopCodes' item='code' open='(' close=')' separator=','>" +
            "#{code}" +
            "</foreach>) " +
            "</if>" +
            "</where>" +
            "group by a.time " +
            "order by a.time " +
            "</script>")
    List<EquipStateSnapshotCountDto> countNumByTime(EquipStateSnapshotCountQuery dto);

    @Select("<script>" +
            "select count(b.sn) as num, " +
            "DATE(b.time) as time " +
            "from  " +
            "(" +
            "SELECT * FROM t_equip_state_snapshot a " +
            "<where>" +
            "<if test='runState!=null'>" +
            "and a.run_state = #{runState} " +
            "</if>" +
            "<if test='alarmState!=null'>" +
            "and a.alarm_state = #{alarmState} " +
            "</if>" +
            "<if test='onlineState!=null'>" +
            "and a.online_state = #{onlineState} " +
            "</if>" +
            "<if test='startDate!=null and endDate!=null'>" +
            "and a.time between #{startDate} and #{endDate} " +
            "</if>" +
            "<if test='workshopCodes!=null and workshopCodes.size>0'>" +
            "and a.sn in (select self_code from t_equip where workshop_code in   " +
            "<foreach collection='workshopCodes' item='code' open='(' close=')' separator=','>" +
            "#{code}" +
            "</foreach>) " +
            "</if>" +
            "and a.time = (" +
            "SELECT MAX(time) FROM t_equip_state_snapshot d " +
            "WHERE d.sn = a.sn " +
            "AND DATE(d.time) = DATE(a.time)" +
            ") " +
            "</where>" +
            ") b " +
            "group by time " +
            "order by time " +
            "</script>")
    List<EquipStateSnapshotCountDto> countNumByTimeDay(EquipStateSnapshotCountQuery dto);
}
