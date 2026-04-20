package com.ourexists.mesedge.report.mapper;

import java.util.Map;

public class WinCCDatalistSqlProvider {

    private static final String[] NUMERIC_COLUMNS = new String[]{
            "o_d11_aerobic_tank_d_o:OD11AerobicTankDO",
            "o_d11_aerobic_tank_nitrification_and_ammonium:OD11AerobicTankNitrificationAndAmmonium",
            "o_d11_aerobic_tank_o_r_p1:OD11AerobicTankORP1",
            "o_d11_anaerobic_tank_o_r_p:OD11AnaerobicTankORP",
            "o_d11_aerobic_tank_o_r_p2:OD11AerobicTankORP2",
            "o_d11_anoxic_tank_d_o:OD11AnoxicTankDO",
            "o_d11_anoxic_tank_o_r_p:OD11AnoxicTankORP",
            "o_d11_sludge_concentration_in_aerobic_tank:OD11SludgeConcentrationInAerobicTank",
            "o_d12_aerobic_tank_d_o:OD12AerobicTankDO",
            "o_d12_aerobic_tank_nitrification_and_ammonium:OD12AerobicTankNitrificationAndAmmonium",
            "o_d12_aerobic_tank_o_r_p1:OD12AerobicTankORP1",
            "o_d12_aerobic_tank_o_r_p2:OD12AerobicTankORP2",
            "o_d12_anaerobic_tank_o_r_p:OD12AnaerobicTankORP",
            "o_d12_anoxic_tank_d_o:OD12AnoxicTankDO",
            "o_d12_anoxic_tank_o_r_p:OD12AnoxicTankORP",
            "o_d12_secondary_clarifier_sludge:OD12SecondaryClarifierSludge",
            "o_d12_sludge_concentration_in_aerobic_tank:OD12SludgeConcentrationInAerobicTank",
            "o_d20_aerobic_tank_d_o:OD20AerobicTankDO",
            "o_d20_aerobic_tank_nitrification_and_ammonium:OD20AerobicTankNitrificationAndAmmonium",
            "o_d20_aerobic_tank_o_r_p1:OD20AerobicTankORP1",
            "o_d20_aerobic_tank_o_r_p2:OD20AerobicTankORP2",
            "o_d20_anaerobic_tank_o_r_p:OD20AnaerobicTankORP",
            "o_d20_anoxic_tank_d_o:OD20AnoxicTankDO",
            "o_d20_anoxic_tank_o_r_p:OD20AnoxicTankORP",
            "o_d20_secondary_clarifier_sludge:OD20SecondaryClarifierSludge",
            "o_d20_sludge_concentration_in_aerobic_tank:OD20SludgeConcentrationInAerobicTank",
            "w_p_sflow:WPSflow",
            "w_p_sinletflow1:WPSinletflow1",
            "w_p_sinletflow2:WPSinletflow2",
            "w_p_sinletflow3:WPSinletflow3",
            "w_p_sinletflow4:WPSinletflow4",
            "w_p_sinletflow5:WPSinletflow5",
            "w_p_sinletflow_total:WPSinletflowTotal",
            "w_p_sinletph:WPSinletph",
            "w_p_sinletss:WPSinletss",
            "w_p_soutletflow:WPSoutletflow",
            "wps_outlet_flow_total:wpsOutletFlowTotal",
            "s107_cod:s107Cod",
            "s107_in_nh3:s107InNh3",
            "wps_total_flow1:wpsTotalFlow1",
            "wps_total_flow2:wpsTotalFlow2",
            "wps_total_flow3:wpsTotalFlow3",
            "wps_total_flow4:wpsTotalFlow4",
            "wps_total_flow5:wpsTotalFlow5",
            "wps_coarse_screen1:wpsCoarseScreen1",
            "wps_coarse_screen2:wpsCoarseScreen2",
            "wps_coarse_screen3:wpsCoarseScreen3",
            "wps_coarse_screen4:wpsCoarseScreen4",
            "wps_fine_grid1:wpsFineGrid1",
            "wps_fine_grid2:wpsFineGrid2",
            "wps_fine_grid3:wpsFineGrid3",
            "wps_fine_grid4:wpsFineGrid4",
            "s108_nh3:s108Nh3",
            "s108_out_cod:s108OutCod",
            "s108_out_ph:s108OutPh",
            "s108_out_tn:s108OutTn",
            "s108_out_th:s108OutTh",
            "wps_total_flow_total:wpsTotalFlowTotal",
            "o_d11_secondary_clarifier_sludge:OD11SecondaryClarifierSludge",
            "inlet_in_tn:inletInTn",
            "inlet_in_tp:inletInTp"
    };

    public String aggregateByPageQuery(Map<String, Object> params) {
        String agg = String.valueOf(params.get("agg")).toUpperCase();
        if (!"AVG".equals(agg) && !"MAX".equals(agg) && !"MIN".equals(agg)) {
            throw new IllegalArgumentException("unsupported agg: " + agg);
        }
        StringBuilder sql = new StringBuilder();
        sql.append("<script>");
        sql.append("select ");
        for (int i = 0; i < NUMERIC_COLUMNS.length; i++) {
            String[] pair = NUMERIC_COLUMNS[i].split(":");
            sql.append(agg).append("(b.").append(pair[1]).append(") as ").append(pair[1]);
            if (i < NUMERIC_COLUMNS.length - 1) {
                sql.append(", ");
            }
        }
        sql.append(" from (");
        sql.append("select ");
        for (int i = 0; i < NUMERIC_COLUMNS.length; i++) {
            String[] pair = NUMERIC_COLUMNS[i].split(":");
            sql.append("a.").append(pair[0]).append(" as ").append(pair[1]);
            if (i < NUMERIC_COLUMNS.length - 1) {
                sql.append(", ");
            }
        }
        sql.append(", a.exec_time as execTime, a.id as id from t_wincc_datalist a ");
        sql.append("where exec_time &gt;= #{dto.startDate} and exec_time &lt;= #{dto.endDate} ");
        sql.append(") b ");
        sql.append("<if test='dto.interval==3'>");
        sql.append("JOIN (SELECT date_trunc('hour', exec_time) AS hour_time, MAX(id) AS max_id ");
        sql.append("FROM t_wincc_datalist where exec_time &gt;= #{dto.startDate} and exec_time &lt;= #{dto.endDate} ");
        sql.append("GROUP BY hour_time) x ON date_trunc('hour', b.execTime) = x.hour_time AND b.id = x.max_id ");
        sql.append("</if>");
        sql.append("<if test='dto.interval==1'>");
        sql.append("JOIN (SELECT date_trunc('hour', exec_time) AS hour_part, ");
        sql.append("FLOOR(EXTRACT(MINUTE FROM exec_time)/10) AS ten_min_part, MAX(id) AS max_id ");
        sql.append("FROM t_wincc_datalist where exec_time &gt;= #{dto.startDate} and exec_time &lt;= #{dto.endDate} ");
        sql.append("GROUP BY hour_part, ten_min_part) x ");
        sql.append("ON date_trunc('hour', b.execTime) = x.hour_part ");
        sql.append("AND FLOOR(EXTRACT(MINUTE FROM b.execTime)/10) = x.ten_min_part AND b.id = x.max_id ");
        sql.append("</if>");
        sql.append("<if test='dto.interval==2'>");
        sql.append("JOIN (SELECT date_trunc('hour', exec_time) AS hour_part, ");
        sql.append("FLOOR(EXTRACT(MINUTE FROM exec_time)/30) AS ten_min_part, MAX(id) AS max_id ");
        sql.append("FROM t_wincc_datalist where exec_time &gt;= #{dto.startDate} and exec_time &lt;= #{dto.endDate} ");
        sql.append("GROUP BY hour_part, ten_min_part) x ");
        sql.append("ON date_trunc('hour', b.execTime) = x.hour_part ");
        sql.append("AND FLOOR(EXTRACT(MINUTE FROM b.execTime)/30) = x.ten_min_part AND b.id = x.max_id ");
        sql.append("</if>");
        sql.append("</script>");
        return sql.toString();
    }
}
