package com.ourexists.mesedge.report.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class WinCCMagDevDto {

    private String id;
    private Boolean magBackupCircuit;
    private Boolean magCharge1;
    private Boolean magCharge2;
    private Boolean magFlocculation1;
    private Boolean magFlocculation2;
    private Boolean magMixer1;
    private Boolean magMixer2;
    private Boolean magResidualPump1;
    private Boolean magResidualPump2;
    private Boolean magReturnPump11;
    private Boolean magReturnPump12;
    private Boolean magReturnPump21;
    private Boolean magReturnPump22;
    private Boolean magScraper1;
    private Boolean magScraper2;
    private Boolean magSeparator1;
    private Boolean magSeparator2;
    private Boolean magSewagePump;
    private Boolean magShear1;
    private Boolean magShear2;
    private Boolean magSludgeConveyingPump01;
    private Boolean magSludgeConveyingPump02;
    private Date startTime;
    private Date endTime;
    private Date execTime;
}
