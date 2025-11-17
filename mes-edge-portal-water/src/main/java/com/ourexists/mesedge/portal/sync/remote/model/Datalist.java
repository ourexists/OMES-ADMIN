package com.ourexists.mesedge.portal.sync.remote.model;

import com.ourexists.mesedge.report.model.WinCCDatalistDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Getter
@Setter
public class Datalist {

    private Float OD11AerobicTankDO;
    private Float OD11AerobicTankNitrificationAndAmmonium;
    private Float OD11AerobicTankORP1;
    private Float OD11AnaerobicTankORP;
    private Float OD11AerobicTankORP2;
    private Float OD11AnoxicTankDO;
    private Float OD11AnoxicTankORP;
    private Float OD11SludgeConcentrationInAerobicTank;
    private Float OD11SecondaryClarifierSludge;
    private Float OD12AerobicTankDO;
    private Float OD12AerobicTankNitrificationAndAmmonium;
    private Float OD12AerobicTankORP1;
    private Float OD12AerobicTankORP2;
    private Float OD12AnaerobicTankORP;
    private Float OD12AnoxicTankDO;
    private Float OD12AnoxicTankORP;
    private Float OD12SecondaryClarifierSludge;
    private Float OD12SludgeConcentrationInAerobicTank;
    private Float OD20AerobicTankDO;
    private Float OD20AerobicTankNitrificationAndAmmonium;
    private Float OD20AerobicTankORP1;
    private Float OD20AerobicTankORP2;
    private Float OD20AnaerobicTankORP;
    private Float OD20AnoxicTankDO;
    private Float OD20AnoxicTankORP;
    private Float OD20SecondaryClarifierSludge;
    private Float OD20SludgeConcentrationInAerobicTank;
    private Float WPSflow;
    private Float WPSinletflow1 = 0f;
    private Float WPSinletflow2 = 0f;
    private Float WPSinletflow3 = 0f;
    private Float WPSinletflow4 = 0f;
    private Float WPSinletflow5 = 0f;
    private Float WPSinletph;
    private Float WPSinletss;
    private Float WPSoutletflow;
    private Float wpsOutletFlowTotal = 0f;
    private Float s107Cod;
    private Float s107InNh3;
    private Float inletInTn;
    private Float inletInTp;
    private Float wpsTotalFlow1 = 0f;
    private Float wpsTotalFlow2 = 0f;
    private Float wpsTotalFlow3 = 0f;
    private Float wpsTotalFlow4 = 0f;
    private Float wpsTotalFlow5 = 0f;
    private Float wpsCoarseScreen1;
    private Float wpsCoarseScreen2;
    private Float wpsCoarseScreen3;
    private Float wpsCoarseScreen4;
    private Float wpsFineGrid1;
    private Float wpsFineGrid2;
    private Float wpsFineGrid3;
    private Float wpsFineGrid4;
    private Float s108Nh3;
    private Float s108OutCod;
    private Float s108OutPh;
    private Float s108OutTn;
    private Float s108OutTh;
    private Date startTime;
    private Date endTime;
    private Date execTime;
    private Float od12InputFowRate;
    private Float od20InputFowRate;
    private Float od12InputletCumulativeFlow;
    private Float od20InputletCumulativeFlow;

    public static WinCCDatalistDto covert(Datalist datalist) {
        WinCCDatalistDto dto = new WinCCDatalistDto();
        BeanUtils.copyProperties(datalist, dto);
        dto.setWpsTotalFlowTotal(datalist.getOd12InputletCumulativeFlow() + datalist.getOd20InputletCumulativeFlow());
        dto.setWPSinletflowTotal(datalist.getOd12InputFowRate() + datalist.getOd20InputFowRate() * 3.6f);
        return dto;
    }
}
