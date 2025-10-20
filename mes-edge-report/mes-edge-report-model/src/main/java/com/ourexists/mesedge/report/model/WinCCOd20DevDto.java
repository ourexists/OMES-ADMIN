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
    private Boolean od20ExcessSludgePump;
    private Boolean od20FillerPusher1;
    private Boolean od20FillerPusher2;
    private Boolean od20FillerPusher3;
    private Boolean od20FillerPusher4;
    private Boolean od20NitrifyingLiquidRefluxPump1;
    private Boolean od20NitrifyingLiquidRefluxPump2;
    private Boolean od20NitrifyingLiquidRefluxPump3;
    private Boolean od20SludgeReturnPump1;
    private Boolean od20SludgeReturnPump2;
    private Boolean od20SludgeReturnPump3;
    private Boolean od20SubmersibleMixer1;
    private Boolean od20SubmersibleMixer2;
    private Boolean od20SubmersibleMixer3;
    private Boolean od20SubmersibleMixer4;
    private Boolean od20SubmersibleMixer5;
    private Boolean od20SubmersibleMixer6;
    private Boolean od20SubmersibleMixer7;
    private Boolean od20SubmersibleMixer8;
    private Boolean od20SubmersibleMixer9;
    private Boolean od20SuctionDredge;
    private Date startTime;
    private Date endTime;
    private Date execTime;
}
