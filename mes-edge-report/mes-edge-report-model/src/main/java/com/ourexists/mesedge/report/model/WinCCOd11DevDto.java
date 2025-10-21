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
    private Integer od11ExcessSludgePump;
    private Integer od11FillerPusher1;
    private Integer od11FillerPusher2;
    private Integer od11FillerPusher3;
    private Integer od11FillerPusher4;
    private Integer od11NitrifyingLiquidRefluxPump1;
    private Integer od11NitrifyingLiquidRefluxPump2;
    private Integer od11NitrifyingLiquidRefluxPump3;
    private Integer od11SludgeReturnPump1;
    private Integer od11SludgeReturnPump2;
    private Integer od11SludgeReturnPump3;
    private Integer od11SubmersibleMixer1;
    private Integer od11SubmersibleMixer2;
    private Integer od11SubmersibleMixer3;
    private Integer od11SubmersibleMixer4;
    private Integer od11SubmersibleMixer5;
    private Integer od11SuctionDredge;
    private Date startTime;
    private Date endTime;
    private Date execTime;
}
