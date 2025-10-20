package com.ourexists.mesedge.report.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class WinCCWpsDevDto {

    private String id;
    private Boolean wpsCirculatingCleaning1;
    private Boolean wpsSandPump1;
    private Boolean wpsBarScreen1;
    private Boolean wpsMixer1;
    private Boolean wpsSewagePump1;
    private Boolean wpsScrewConveyor1;
    private Boolean wpsCirculatingCleaning2;
    private Boolean wpsSandPump2;
    private Boolean wpsBarScreen2;
    private Boolean wpsMixer2;
    private Boolean wpsSewagePump2;
    private Boolean wpsScrewConveyor2;
    private Boolean wpsSewagePump3;
    private Boolean wpsSewagePump4;
    private Boolean wpsSewagePump5;
    private Boolean wpsSandSeparator;
    private Date startTime;
    private Date endTime;
    private Date execTime;
}
