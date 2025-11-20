package com.ourexists.mesedge.portal.sync.remote.model;

import com.ourexists.mesedge.portal.sync.remote.WinCCVari;
import com.ourexists.mesedge.report.model.WinCCOd11DevDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class Od11DevVari {
    @WinCCVari("OD11_ExcessSludgePump")
    private Integer od11ExcessSludgePump;

    @WinCCVari("OD11_FillerPusher1")
    private Integer od11FillerPusher1;

    @WinCCVari("OD11_FillerPusher2")
    private Integer od11FillerPusher2;

    @WinCCVari("OD11_FillerPusher3")
    private Integer od11FillerPusher3;

    @WinCCVari("OD11_FillerPusher4")
    private Integer od11FillerPusher4;

    @WinCCVari("OD11_NitrifyingLiquidRefluxPump1")
    private Integer od11NitrifyingLiquidRefluxPump1;

    @WinCCVari("OD11_NitrifyingLiquidRefluxPump2")
    private Integer od11NitrifyingLiquidRefluxPump2;

    @WinCCVari("OD11_NitrifyingLiquidRefluxPump3")
    private Integer od11NitrifyingLiquidRefluxPump3;

    @WinCCVari("OD11_SludgeReturnPump1")
    private Integer od11SludgeReturnPump1;

    @WinCCVari("OD11_SludgeReturnPump2")
    private Integer od11SludgeReturnPump2;

    @WinCCVari("OD11_SludgeReturnPump3")
    private Integer od11SludgeReturnPump3;

    @WinCCVari("OD11_SubmersibleMixer1")
    private Integer od11SubmersibleMixer1;

    @WinCCVari("OD11_SubmersibleMixer2")
    private Integer od11SubmersibleMixer2;

    @WinCCVari("OD11_SubmersibleMixer3")
    private Integer od11SubmersibleMixer3;

    @WinCCVari("OD11_SubmersibleMixer4")
    private Integer od11SubmersibleMixer4;

    @WinCCVari("OD11_SubmersibleMixer5")
    private Integer od11SubmersibleMixer5;

    @WinCCVari("OD11_SuctionDredge")
    private Integer od11SuctionDredge;

    public static WinCCOd11DevDto covert(Od11DevVari source) {
        WinCCOd11DevDto target = new WinCCOd11DevDto();
        BeanUtils.copyProperties(source, target);
        target.setExecTime(new Date());
        return target;
    }
}
