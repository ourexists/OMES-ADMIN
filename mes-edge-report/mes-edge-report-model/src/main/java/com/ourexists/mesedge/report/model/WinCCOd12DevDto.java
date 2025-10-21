package com.ourexists.mesedge.report.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class WinCCOd12DevDto {
    private String id;

    private Integer od12ExcessSludgePump;
    private Integer od12FillerPusher1;
    private Integer od12FillerPusher2;
    private Integer od12FillerPusher3;
    private Integer od12FillerPusher4;
    private Integer od12NitrifyingLiquidRefluxPump1;
    private Integer od12NitrifyingLiquidRefluxPump2;
    private Integer od12NitrifyingLiquidRefluxPump3;
    private Integer od12SludgeReturnPump1;
    private Integer od12SludgeReturnPump2;
    private Integer od12SludgeReturnPump3;
    private Integer od12SubmersibleMixer1;
    private Integer od12SubmersibleMixer2;
    private Integer od12SubmersibleMixer3;
    private Integer od12SubmersibleMixer4;
    private Integer od12SubmersibleMixer5;
    private Integer od12SuctionDredge;
    private Date startTime;
    private Date endTime;
    private Date execTime;
}
