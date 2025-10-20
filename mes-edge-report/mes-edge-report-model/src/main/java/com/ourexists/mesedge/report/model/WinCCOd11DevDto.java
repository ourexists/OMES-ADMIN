package com.ourexists.mesedge.report.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class WinCCOd11DevDto {
    private String id;
    private Boolean od11ExcessSludgePump;
    private Boolean od11FillerPusher1;
    private Boolean od11FillerPusher2;
    private Boolean od11FillerPusher3;
    private Boolean od11FillerPusher4;
    private Boolean od11NitrifyingLiquidRefluxPump1;
    private Boolean od11NitrifyingLiquidRefluxPump2;
    private Boolean od11NitrifyingLiquidRefluxPump3;
    private Boolean od11SludgeReturnPump1;
    private Boolean od11SludgeReturnPump2;
    private Boolean od11SludgeReturnPump3;
    private Boolean od11SubmersibleMixer1;
    private Boolean od11SubmersibleMixer2;
    private Boolean od11SubmersibleMixer3;
    private Boolean od11SubmersibleMixer4;
    private Boolean od11SubmersibleMixer5;
    private Boolean od11SuctionDredge;
    private Date startTime;
    private Date endTime;
    private Date execTime;
}
