/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.mesedge.report.model.WinCCDatalist;
import com.ourexists.mesedge.report.model.WinCCDatalistPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
@Mapper
public interface WinCCDatalistMapper extends BaseMapper<WinCCDatalist> {

    @Select("<script>" +
            "select * from (" +
            "select  " +
            "a.o_d11_aerobic_tank_d_o as OD11AerobicTankDO, " +
            "a.o_d11_aerobic_tank_nitrification_and_ammonium as OD11AerobicTankNitrificationAndAmmonium," +
            "a.o_d11_aerobic_tank_o_r_p1 as OD11AerobicTankORP1," +
            "a.o_d11_anaerobic_tank_o_r_p as OD11AnaerobicTankORP," +
            "a.o_d11_aerobic_tank_o_r_p2 as OD11AerobicTankORP2," +
            "a.o_d11_anoxic_tank_d_o as OD11AnoxicTankDO," +
            "a.o_d11_anoxic_tank_o_r_p as OD11AnoxicTankORP," +
            "a.o_d11_sludge_concentration_in_aerobic_tank as OD11SludgeConcentrationInAerobicTank," +
            "a.o_d12_aerobic_tank_d_o as OD12AerobicTankDO," +
            "a.o_d12_aerobic_tank_nitrification_and_ammonium as OD12AerobicTankNitrificationAndAmmonium," +
            "a.o_d12_aerobic_tank_o_r_p1 as OD12AerobicTankORP1," +
            "a.o_d12_aerobic_tank_o_r_p2 as OD12AerobicTankORP2," +
            "a.o_d12_anaerobic_tank_o_r_p as OD12AnaerobicTankORP," +
            "a.o_d12_anoxic_tank_d_o as OD12AnoxicTankDO," +
            "a.o_d12_anoxic_tank_o_r_p as OD12AnoxicTankORP," +
            "a.o_d12_secondary_clarifier_sludge as OD12SecondaryClarifierSludge," +
            "a.o_d12_sludge_concentration_in_aerobic_tank as OD12SludgeConcentrationInAerobicTank," +
            "a.o_d20_aerobic_tank_d_o as OD20AerobicTankDO," +
            "a.o_d20_aerobic_tank_nitrification_and_ammonium as OD20AerobicTankNitrificationAndAmmonium," +
            "a.o_d20_aerobic_tank_o_r_p1 as OD20AerobicTankORP1," +
            "a.o_d20_aerobic_tank_o_r_p2 as OD20AerobicTankORP2," +
            "a.o_d20_anaerobic_tank_o_r_p as OD20AnaerobicTankORP," +
            "a.o_d20_anoxic_tank_d_o as OD20AnoxicTankDO," +
            "a.o_d20_anoxic_tank_o_r_p as OD20AnoxicTankORP," +
            "a.o_d20_secondary_clarifier_sludge as OD20SecondaryClarifierSludge," +
            "a.o_d20_sludge_concentration_in_aerobic_tank as OD20SludgeConcentrationInAerobicTank," +
            "a.w_p_sflow as WPSflow," +
            "a.w_p_sinletflow1 as WPSinletflow1," +
            "a.w_p_sinletflow2 as WPSinletflow2," +
            "a.w_p_sinletflow3 as WPSinletflow3," +
            "a.w_p_sinletflow4 as WPSinletflow4," +
            "a.w_p_sinletflow5 as WPSinletflow5," +
            "a.w_p_sinletflow_total as WPSinletflowTotal," +
            "a.w_p_sinletph as WPSinletph," +
            "a.w_p_sinletss as WPSinletss," +
            "a.w_p_soutletflow as WPSoutletflow," +
            "a.wps_outlet_flow_total as wpsOutletFlowTotal," +
            "a.s107_cod as s107Cod," +
            "a.start_time as startTime," +
            "a.end_time as endTime," +
            "a.exec_time as execTime," +
            "a.id," +
            "a.created_by as createdBy," +
            "a.created_id as createdId," +
            "a.created_time as createdTime," +
            "a.updated_by as updatedBy," +
            "a.updated_id as updatedId," +
            "a.updated_time as updatedTime," +
            "a.tenant_id as tenantId," +
            "a.s107_in_nh3 as s107InNh3," +
            "a.wps_total_flow1 as wpsTotalFlow1," +
            "a.wps_total_flow2 as wpsTotalFlow2," +
            "a.wps_total_flow3 as wpsTotalFlow3," +
            "a.wps_total_flow4 as wpsTotalFlow4," +
            "a.wps_total_flow5 as wpsTotalFlow5," +
            "a.wps_coarse_screen1 as wpsCoarseScreen1," +
            "a.wps_coarse_screen2 as wpsCoarseScreen2," +
            "a.wps_coarse_screen3 as wpsCoarseScreen3," +
            "a.wps_coarse_screen4 as wpsCoarseScreen4," +
            "a.wps_fine_grid1 as wpsFineGrid1," +
            "a.wps_fine_grid2 as wpsFineGrid2," +
            "a.wps_fine_grid3 as wpsFineGrid3," +
            "a.wps_fine_grid4 as wpsFineGrid4," +
            "a.s108_nh3 as s108Nh3," +
            "a.s108_out_cod as s108OutCod," +
            "a.s108_out_ph as s108OutPh," +
            "a.s108_out_tn as s108OutTn," +
            "a.s108_out_th as s108OutTh," +
            "a.wps_total_flow_total as wpsTotalFlowTotal," +
            "a.o_d11_secondary_clarifier_sludge as OD11SecondaryClarifierSludge," +
            "a.inlet_in_tn as inletInTn," +
            "a.inlet_in_tp as inletInTp from t_wincc_datalist a " +
            "where exec_time &gt;= #{startDate} and exec_time &lt;= #{endDate} " +
            "order by id desc " +
            ") b " +

            "<if test=\"interval==3\">" +
            " JOIN (" +
            "SELECT DATE_FORMAT(exec_time, '%Y-%m-%d %H:00:00') AS hour_time," +
            "MAX(id) AS max_id " +
            "FROM t_wincc_datalist " +
            "where exec_time &gt;= #{startDate} and exec_time &lt;= #{endDate} " +
            "GROUP BY hour_time) x " +
            "ON DATE_FORMAT(b.execTime, '%Y-%m-%d %H:00:00') = x.hour_time " +
            "AND b.id = x.max_id " +
            "</if>" +

            "<if test=\"interval==1\">" +
            " JOIN (" +
            "SELECT " +
            "DATE_FORMAT(exec_time, '%Y-%m-%d %H:') AS hour_part," +
            "FLOOR(MINUTE(exec_time)/10) AS ten_min_part," +
            "MAX(id) AS max_id "+
            "FROM t_wincc_datalist " +
            "where exec_time &gt;= #{startDate} and exec_time &lt;= #{endDate} " +
            "GROUP BY hour_part, ten_min_part " +
            ") x " +
            "ON DATE_FORMAT(b.execTime, '%Y-%m-%d %H:') = x.hour_part " +
            "AND FLOOR(MINUTE(b.execTime)/10) = x.ten_min_part " +
            "AND b.id = x.max_id " +
            "</if>" +

            "<if test=\"interval==2\">" +
            " JOIN (" +
            "SELECT " +
            "DATE_FORMAT(exec_time, '%Y-%m-%d %H:') AS hour_part," +
            "FLOOR(MINUTE(exec_time)/30) AS ten_min_part," +
            "MAX(id) AS max_id "+
            "FROM t_wincc_datalist " +
            "where exec_time &gt;= #{startDate} and exec_time &lt;= #{endDate} " +
            "GROUP BY hour_part, ten_min_part " +
            ") x " +
            "ON DATE_FORMAT(b.execTime, '%Y-%m-%d %H:') = x.hour_part " +
            "AND FLOOR(MINUTE(b.execTime)/30) = x.ten_min_part " +
            "AND b.id = x.max_id " +
            "</if>" +
            "</script>"
    )
    List<WinCCDatalist> page(WinCCDatalistPageQuery dto);

}
