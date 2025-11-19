package com.ourexists.mesedge.report.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class WinCCZsDevDto {

    private String id;

    private Integer csdDosingPump1;
    private Integer csdDosingPump2;
    private Integer csdDosingPump3;
    private Integer csdHyPump1;
    private Integer csdHyPump3;
    private Integer csdHyPump4;
    private Integer csdHyPump5;
    private Integer csdHyPump20;
    private Integer csdHyPump21;
    private Integer csdJbq1;
    private Integer csdJbq2;
    private Integer csdTsPump1;
    private Integer csdTsPump2;
    private Integer csdTsPump3;
    private Integer csdTsPump4;
    private Integer csdUnloadingPump;
    private Integer nacioDosingPump1;
    private Integer nacioDosingPump2;
    private Integer nacioUnloadingPump;

    private Date startTime;
    private Date endTime;
    private Date execTime;

}
