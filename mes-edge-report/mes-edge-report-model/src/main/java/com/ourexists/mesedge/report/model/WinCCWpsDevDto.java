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

    private Integer wpsSandPump1;
    private Integer wpsSandPump2;
    private Integer wpsBarScreen1;
    private Integer wpsBarScreen2;
    private Integer wpsMixer1;
    private Integer wpsMixer2;
    private Integer wpsSewagePump1;
    private Integer wpsSewagePump2;
    private Integer wpsSewagePump3;
    private Integer wpsSewagePump4;
    private Integer wpsSewagePump5;
    private Integer wpsScrewConveyor1;
    private Integer wpsScrewConveyor2;
    private Integer wpsCirculatingCleaning1;
    private Integer wpsCirculatingCleaning2;
    private Integer wpsSandSeparator;
    private Date startTime;
    private Date endTime;
    private Date execTime;
}
