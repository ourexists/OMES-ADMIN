package com.ourexists.mesedge.portal.sync.remote.model;

import com.ourexists.mesedge.portal.sync.remote.WinCCVari;
import com.ourexists.mesedge.report.model.WinCCOd20DevDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class Od20DevVari {

    @WinCCVari("OD20_ExcessSludgePump")
    private Integer od20ExcessSludgePump;

    @WinCCVari("OD20_FillerPusher1")
    private Integer od20FillerPusher1;

    @WinCCVari("OD20_FillerPusher2")
    private Integer od20FillerPusher2;

    @WinCCVari("OD20_FillerPusher3")
    private Integer od20FillerPusher3;

    @WinCCVari("OD20_FillerPusher4")
    private Integer od20FillerPusher4;

    @WinCCVari("OD20_NitrifyingLiquidRefluxPump1")
    private Integer od20NitrifyingLiquidRefluxPump1;

    @WinCCVari("OD20_NitrifyingLiquidRefluxPump2")
    private Integer od20NitrifyingLiquidRefluxPump2;

    @WinCCVari("OD20_NitrifyingLiquidRefluxPump3")
    private Integer od20NitrifyingLiquidRefluxPump3;

    @WinCCVari("OD20_SludgeReturnPump1")
    private Integer od20SludgeReturnPump1;

    @WinCCVari("OD20_SludgeReturnPump2")
    private Integer od20SludgeReturnPump2;

    @WinCCVari("OD20_SludgeReturnPump3")
    private Integer od20SludgeReturnPump3;

    @WinCCVari("OD20_SubmersibleMixer1")
    private Integer od20SubmersibleMixer1;

    @WinCCVari("OD20_SubmersibleMixer2")
    private Integer od20SubmersibleMixer2;

    @WinCCVari("OD20_SubmersibleMixer3")
    private Integer od20SubmersibleMixer3;

    @WinCCVari("OD20_SubmersibleMixer4")
    private Integer od20SubmersibleMixer4;

    @WinCCVari("OD20_SubmersibleMixer5")
    private Integer od20SubmersibleMixer5;

    @WinCCVari("OD20_SubmersibleMixer6")
    private Integer od20SubmersibleMixer6;

    @WinCCVari("OD20_SubmersibleMixer7")
    private Integer od20SubmersibleMixer7;

    @WinCCVari("OD20_SubmersibleMixer8")
    private Integer od20SubmersibleMixer8;

    @WinCCVari("OD20_SubmersibleMixer9")
    private Integer od20SubmersibleMixer9;

    @WinCCVari("OD20_SuctionDredge")
    private Integer od20SuctionDredge;

    public static WinCCOd20DevDto covert(Od20DevVari source) {
        WinCCOd20DevDto target = new WinCCOd20DevDto();
        BeanUtils.copyProperties(source, target);
        target.setExecTime(new Date());
        return target;
    }
}
