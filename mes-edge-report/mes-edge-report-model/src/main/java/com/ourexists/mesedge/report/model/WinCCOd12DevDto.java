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

    private Boolean od12ExcessSludgePump;
    private Boolean od12FillerPusher1;
    private Boolean od12FillerPusher2;
    private Boolean od12FillerPusher3;
    private Boolean od12FillerPusher4;
    private Boolean od12NitrifyingLiquidRefluxPump1;
    private Boolean od12NitrifyingLiquidRefluxPump2;
    private Boolean od12NitrifyingLiquidRefluxPump3;
    private Boolean od12SludgeReturnPump1;
    private Boolean od12SludgeReturnPump2;
    private Boolean od12SludgeReturnPump3;
    private Boolean od12SubmersibleMixer1;
    private Boolean od12SubmersibleMixer2;
    private Boolean od12SubmersibleMixer3;
    private Boolean od12SubmersibleMixer4;
    private Boolean od12SubmersibleMixer5;
    private Boolean od12SuctionDredge;
    private Date startTime;
    private Date endTime;
    private Date execTime;
}
