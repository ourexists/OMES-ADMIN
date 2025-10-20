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
    private Boolean pamBlender1;
    private Boolean pamBlender2;
    private Boolean pamDispensing;
    private Boolean pamHeater;
    private Boolean pamScrewPump1;
    private Boolean pamScrewPump2;
    private Boolean pamScrewPump3;
    private Boolean pamVibrator;
    private Boolean pamWaterInlet;
    private Boolean pfsDosingPumpA;
    private Boolean pfsDosingPumpB;
    private Boolean pfsDosingPumpC;
    private Boolean pfsUnloadingPump;
    private Boolean naac1DosingPumpA;
    private Boolean naac1DosingPumpB;
    private Boolean naac1DosingPumpC;
    private Boolean naac1UnloadingPump;
    private Boolean naac2DosingPumpA;
    private Boolean naac2DosingPumpB;
    private Date startTime;
    private Date endTime;
    private Date execTime;
}
