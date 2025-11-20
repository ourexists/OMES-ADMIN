package com.ourexists.mesedge.portal.sync.remote.model;

import com.ourexists.mesedge.portal.sync.remote.WinCCVari;
import com.ourexists.mesedge.report.model.WinCCOd12DevDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class Od12DevVari {

    @WinCCVari("OD12_ExcessSludgePump")
    private Integer od12ExcessSludgePump;

    @WinCCVari("OD12_FillerPusher1")
    private Integer od12FillerPusher1;

    @WinCCVari("OD12_FillerPusher2")
    private Integer od12FillerPusher2;

    @WinCCVari("OD12_FillerPusher3")
    private Integer od12FillerPusher3;

    @WinCCVari("OD12_FillerPusher4")
    private Integer od12FillerPusher4;

    @WinCCVari("OD12_NitrifyingLiquidRefluxPump1")
    private Integer od12NitrifyingLiquidRefluxPump1;

    @WinCCVari("OD12_NitrifyingLiquidRefluxPump2")
    private Integer od12NitrifyingLiquidRefluxPump2;

    @WinCCVari("OD12_NitrifyingLiquidRefluxPump3")
    private Integer od12NitrifyingLiquidRefluxPump3;

    @WinCCVari("OD12_SludgeReturnPump1")
    private Integer od12SludgeReturnPump1;

    @WinCCVari("OD12_SludgeReturnPump2")
    private Integer od12SludgeReturnPump2;

    @WinCCVari("OD12_SludgeReturnPump3")
    private Integer od12SludgeReturnPump3;

    @WinCCVari("OD12_SubmersibleMixer1")
    private Integer od12SubmersibleMixer1;

    @WinCCVari("OD12_SubmersibleMixer2")
    private Integer od12SubmersibleMixer2;

    @WinCCVari("OD12_SubmersibleMixer3")
    private Integer od12SubmersibleMixer3;

    @WinCCVari("OD12_SubmersibleMixer4")
    private Integer od12SubmersibleMixer4;

    @WinCCVari("OD12_SubmersibleMixer5")
    private Integer od12SubmersibleMixer5;

    @WinCCVari("OD12_SuctionDredge")
    private Integer od12SuctionDredge;

    public static WinCCOd12DevDto covert(Od12DevVari source) {
        WinCCOd12DevDto target = new WinCCOd12DevDto();
        BeanUtils.copyProperties(source, target);
        target.setExecTime(new Date());
        return target;
    }
}
