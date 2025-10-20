package com.ourexists.mesedge.report.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class WinCCDatalistDto {

    private String id;
    private Float OD11AerobicTankDO;
    private Float OD11AerobicTankNitrificationAndAmmonium;
    private Float OD11AerobicTankORP1;
    private Float OD11AnaerobicTankORP;
    private Float OD11AnaerobicTankORP1;
    private Float OD11AnoxicTankDO;
    private Float OD11AnoxicTankORP;
    private Float OD11SludgeConcentrationInAerobicTank;
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
    private Float WPSinletflow1;
    private Float WPSinletflow2;
    private Float WPSinletflow3;
    private Float WPSinletflow4;
    private Float WPSinletflow5;
    private Float WPSinletph;
    private Float WPSinletss;
    private Float WPSoutletflow;
    private Date startTime;
    private Date endTime;
    private Date execTime;
}
