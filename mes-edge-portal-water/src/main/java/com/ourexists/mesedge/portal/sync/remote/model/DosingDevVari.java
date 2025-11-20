package com.ourexists.mesedge.portal.sync.remote.model;

import com.ourexists.mesedge.portal.sync.remote.WinCCVari;
import com.ourexists.mesedge.report.model.WinCCDosingDevDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class DosingDevVari {

    @WinCCVari("PAM_Blender1")
    private Integer pamBlender1;

    @WinCCVari("PAM_Blender2")
    private Integer pamBlender2;

    @WinCCVari("PAM_Dispensing")
    private Integer pamDispensing;

    @WinCCVari("PAM_Heater")
    private Integer pamHeater;

    @WinCCVari("PAM_ScrewPump1")
    private Integer pamScrewPump1;

    @WinCCVari("PAM_ScrewPump2")
    private Integer pamScrewPump2;

    @WinCCVari("PAM_ScrewPump3")
    private Integer pamScrewPump3;

    @WinCCVari("PAM_Vibrator")
    private Integer pamVibrator;

    @WinCCVari("PAM_WaterInlet")
    private Integer pamWaterInlet;

    @WinCCVari("PFS_DosingPumpA")
    private Integer pfsDosingPumpA;

    @WinCCVari("PFS_DosingPumpB")
    private Integer pfsDosingPumpB;

    @WinCCVari("PFS_DosingPumpC")
    private Integer pfsDosingPumpC;

    @WinCCVari("UnloadingPump")
    private Integer pfsUnloadingPump;

    @WinCCVari("NaAc1_DosingPumpA")
    private Integer naac1DosingPumpA;

    @WinCCVari("NaAc1_DosingPumpB")
    private Integer naac1DosingPumpB;

    @WinCCVari("NaAc1_DosingPumpC")
    private Integer naac1DosingPumpC;

    @WinCCVari("NaAc1_UnloadingPump")
    private Integer naac1UnloadingPump;

    @WinCCVari("NaAc2_DosingPumpA")
    private Integer naac2DosingPumpA;

    @WinCCVari("NaAc2_DosingPumpB")
    private Integer naac2DosingPumpB;

    public static WinCCDosingDevDto covert(DosingDevVari source) {
        WinCCDosingDevDto target = new WinCCDosingDevDto();
        BeanUtils.copyProperties(source, target);
        target.setExecTime(new Date());
        return target;
    }
}
