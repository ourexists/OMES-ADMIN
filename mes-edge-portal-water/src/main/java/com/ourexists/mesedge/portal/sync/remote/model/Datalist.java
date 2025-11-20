package com.ourexists.mesedge.portal.sync.remote.model;

import com.alibaba.fastjson2.annotation.JSONField;
import com.ourexists.mesedge.portal.sync.remote.WinCCVari;
import com.ourexists.mesedge.report.model.WinCCDatalistDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Getter
@Setter
public class Datalist {

    @WinCCVari("OD11_AerobicTankDO")
    private Float OD11AerobicTankDO;

    @WinCCVari("OD11_AerobicTankNitrificationAndAmmonium")
    private Float OD11AerobicTankNitrificationAndAmmonium;

    @WinCCVari("OD11_AerobicTankORP1")
    private Float OD11AerobicTankORP1;

    @WinCCVari("OD11_AnaerobicTankORP")
    private Float OD11AnaerobicTankORP;

    @WinCCVari("OD11_AerobicTankORP2")
    private Float OD11AerobicTankORP2;

    @WinCCVari("OD11_AnoxicTankDO")
    private Float OD11AnoxicTankDO;

    @WinCCVari("OD11_AnoxicTankORP")
    private Float OD11AnoxicTankORP;

    @WinCCVari("OD11_SludgeConcentrationInAerobicTank")
    private Float OD11SludgeConcentrationInAerobicTank;

    @WinCCVari("OD11_SecondaryClarifier\u200CSludge\u200C")
    private Float OD11SecondaryClarifierSludge;

    @WinCCVari("OD12_AerobicTankDO")
    private Float OD12AerobicTankDO;

    @WinCCVari("OD12_AerobicTankNitrificationAndAmmonium")
    private Float OD12AerobicTankNitrificationAndAmmonium;

    @WinCCVari("OD12_AerobicTankORP1")
    private Float OD12AerobicTankORP1;

    @WinCCVari("OD12_AerobicTankORP2")
    private Float OD12AerobicTankORP2;

    @WinCCVari("OD12_AnaerobicTankORP")
    private Float OD12AnaerobicTankORP;

    @WinCCVari("OD12_AnoxicTankDO")
    private Float OD12AnoxicTankDO;

    @WinCCVari("OD12_AnoxicTankORP")
    private Float OD12AnoxicTankORP;

    @WinCCVari("OD12_SecondaryClarifier\u200CSludge\u200C")
    private Float OD12SecondaryClarifierSludge;

    @WinCCVari("OD12_SludgeConcentrationInAerobicTank")
    private Float OD12SludgeConcentrationInAerobicTank;

    @WinCCVari("OD20_AerobicTankDO")
    private Float OD20AerobicTankDO;

    @WinCCVari("OD20_AerobicTankNitrificationAndAmmonium")
    private Float OD20AerobicTankNitrificationAndAmmonium;

    @WinCCVari("OD20_AerobicTankORP1")
    private Float OD20AerobicTankORP1;

    @WinCCVari("OD20_AerobicTankORP2")
    private Float OD20AerobicTankORP2;

    @WinCCVari("OD20_AnaerobicTankORP")
    private Float OD20AnaerobicTankORP;

    @WinCCVari("OD20_AnoxicTankDO")
    private Float OD20AnoxicTankDO;

    @WinCCVari("OD20_AnoxicTankORP")
    private Float OD20AnoxicTankORP;

    @WinCCVari("OD20_SecondaryClarifier\u200CSludge\u200C")
    private Float OD20SecondaryClarifierSludge;

    @WinCCVari("OD20_SludgeConcentrationInAerobicTank")
    private Float OD20SludgeConcentrationInAerobicTank;

    @WinCCVari("提升泵房液位_显示值")
    private Float WPSflow;

    @WinCCVari("1#瞬时流量_显示值")
    private Float WPSinletflow1 = 0f;

    @WinCCVari("2#瞬时流量_显示值")
    private Float WPSinletflow2 = 0f;

    @WinCCVari("3#瞬时流量_显示值")
    private Float WPSinletflow3 = 0f;

    @JSONField(name = "4#瞬时流量_显示值")
    private Float WPSinletflow4 = 0f;

    @WinCCVari("5#进水瞬时流量_显示值")
    private Float WPSinletflow5 = 0f;

    @WinCCVari("旋流沉砂池进水PH_显示值")
    private Float WPSinletph;

    @WinCCVari("旋流沉砂池进水SS_显示值")
    private Float WPSinletss;

    @WinCCVari("水厂出水瞬时流量_显示值")
    private Float WPSoutletflow;

    @WinCCVari("WPS_OutletCumulativeFlowDay")
    private Float wpsOutletFlowTotal = 0f;

    @WinCCVari("S107_COD")
    private Float s107Cod;

    @WinCCVari("S107_IN_NH3")
    private Float s107InNh3;

    @WinCCVari("inlet_in_tn")
    private Float inletInTn;

    @WinCCVari("inlet_in_tp")
    private Float inletInTp;

    @WinCCVari("1#累计流量_显示值")
    private Float wpsTotalFlow1 = 0f;

    @WinCCVari("2#累计流量_显示值")
    private Float wpsTotalFlow2 = 0f;

    @WinCCVari("3#累计流量_显示值")
    private Float wpsTotalFlow3 = 0f;

    @WinCCVari("4#累计流量_显示值")
    private Float wpsTotalFlow4 = 0f;

    @WinCCVari("5#进水累计流量_显示值")
    private Float wpsTotalFlow5 = 0f;

    @WinCCVari("1#粗格栅液位差_显示值")
    private Float wpsCoarseScreen1;

    @WinCCVari("2#粗格栅液位差_显示值")
    private Float wpsCoarseScreen2;

    @WinCCVari("3#粗格栅液位差_显示值")
    private Float wpsCoarseScreen3;

    @WinCCVari("4#粗格栅液位差_显示值")
    private Float wpsCoarseScreen4;

    @WinCCVari("1#细格栅液位差_显示值")
    private Float wpsFineGrid1;

    @WinCCVari("2#细格栅液位差_显示值")
    private Float wpsFineGrid2;

    @WinCCVari("3#细格栅液位差_显示值")
    private Float wpsFineGrid3;

    @WinCCVari("4#细格栅液位差_显示值")
    private Float wpsFineGrid4;

    @WinCCVari("S108_NH3")
    private Float s108Nh3;

    @WinCCVari("S108_OUT_COD")
    private Float s108OutCod;

    @WinCCVari("S108_OUT_PH")
    private Float s108OutPh;

    @WinCCVari("S108_OUT_TN")
    private Float s108OutTn;

    @WinCCVari("S108_OUT_TP")
    private Float s108OutTh;

    @WinCCVari("OD12_InputFowRate")
    private Float od12InputFowRate;

    @WinCCVari("OD20_InputFowRate")
    private Float od20InputFowRate;

    @WinCCVari("OD12_InputCumulativeFlowDay")
    private Float od12InputletCumulativeFlow;

    @WinCCVari("OD20_InputCumulativeFlowDay")
    private Float od20InputletCumulativeFlow;

    public static WinCCDatalistDto covert(Datalist datalist) {
        WinCCDatalistDto dto = new WinCCDatalistDto();
        BeanUtils.copyProperties(datalist, dto);
        dto.setWpsTotalFlowTotal(datalist.getOd12InputletCumulativeFlow() + datalist.getOd20InputletCumulativeFlow());
        dto.setWPSinletflowTotal(datalist.getOd12InputFowRate() + datalist.getOd20InputFowRate() * 3.6f);
        dto.setExecTime(new Date());
        return dto;
    }
}
