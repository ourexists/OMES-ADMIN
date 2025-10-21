package com.ourexists.mesedge.report.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class WinCCOd20DevDto {
    private String id;
    private Integer od20ExcessSludgePump;
    private Integer od20FillerPusher1;
    private Integer od20FillerPusher2;
    private Integer od20FillerPusher3;
    private Integer od20FillerPusher4;
    private Integer od20NitrifyingLiquidRefluxPump1;
    private Integer od20NitrifyingLiquidRefluxPump2;
    private Integer od20NitrifyingLiquidRefluxPump3;
    private Integer od20SludgeReturnPump1;
    private Integer od20SludgeReturnPump2;
    private Integer od20SludgeReturnPump3;
    private Integer od20SubmersibleMixer1;
    private Integer od20SubmersibleMixer2;
    private Integer od20SubmersibleMixer3;
    private Integer od20SubmersibleMixer4;
    private Integer od20SubmersibleMixer5;
    private Integer od20SubmersibleMixer6;
    private Integer od20SubmersibleMixer7;
    private Integer od20SubmersibleMixer8;
    private Integer od20SubmersibleMixer9;
    private Integer od20SuctionDredge;
    private Date startTime;
    private Date endTime;
    private Date execTime;
}
