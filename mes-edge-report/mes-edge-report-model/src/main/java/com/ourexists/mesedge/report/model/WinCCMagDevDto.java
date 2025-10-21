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
    private Integer magBackupCircuit;
    private Integer magCharge1;
    private Integer magCharge2;
    private Integer magFlocculation1;
    private Integer magFlocculation2;
    private Integer magMixer1;
    private Integer magMixer2;
    private Integer magResidualPump1;
    private Integer magResidualPump2;
    private Integer magReturnPump11;
    private Integer magReturnPump12;
    private Integer magReturnPump21;
    private Integer magReturnPump22;
    private Integer magScraper1;
    private Integer magScraper2;
    private Integer magSeparator1;
    private Integer magSeparator2;
    private Integer magSewagePump;
    private Integer magShear1;
    private Integer magShear2;
    private Integer magSludgeConveyingPump01;
    private Integer magSludgeConveyingPump02;
    private Date startTime;
    private Date endTime;
    private Date execTime;
}
