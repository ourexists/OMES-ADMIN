package com.ourexists.mesedge.report.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class WinCCDosingDevDto {
    private String id;
    private Integer pamBlender1;
    private Integer pamBlender2;
    private Integer pamDispensing;
    private Integer pamHeater;
    private Integer pamScrewPump1;
    private Integer pamScrewPump2;
    private Integer pamScrewPump3;
    private Integer pamVibrator;
    private Integer pamWaterInlet;
    private Integer pfsDosingPumpA;
    private Integer pfsDosingPumpB;
    private Integer pfsDosingPumpC;
    private Integer pfsUnloadingPump;
    private Integer naac1DosingPumpA;
    private Integer naac1DosingPumpB;
    private Integer naac1DosingPumpC;
    private Integer naac1UnloadingPump;
    private Integer naac2DosingPumpA;
    private Integer naac2DosingPumpB;
    private Date startTime;
    private Date endTime;
    private Date execTime;
}
