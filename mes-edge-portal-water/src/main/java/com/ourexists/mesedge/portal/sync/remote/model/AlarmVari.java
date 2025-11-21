package com.ourexists.mesedge.portal.sync.remote.model;

import com.ourexists.mesedge.portal.sync.remote.WinCCVari;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlarmVari {

    @WinCCVari("MAG_CatchBasin_Hitch_1.alarm")
    private Integer magCatchBasinHitch1;

    @WinCCVari("MAG_CatchBasin_Hitch_2.alarm")
    private Integer magCatchBasinHitch2;

    @WinCCVari("MAG_CatchBasin_Hitch_3.alarm")
    private Integer magCatchBasinHitch3;

    @WinCCVari("MAG_ResidualPump1_Hitch_1.alarm")
    private Integer magResidualPump1Hitch1;

    @WinCCVari("MAG_ResidualPump1_Hitch_2.alarm")
    private Integer magResidualPump1Hitch2;

    @WinCCVari("MAG_ResidualPump2_Hitch_1.alarm")
    private Integer magResidualPump2Hitch1;

    @WinCCVari("MAG_ResidualPump2_Hitch_2.alarm")
    private Integer magResidualPump2Hitch2;

    @WinCCVari("MAG_ReturnPump11_Hitch_1.alarm")
    private Integer magReturnPump11Hitch;

    @WinCCVari("MAG_ReturnPump12_Hitch_1.alarm")
    private Integer magReturnPump12Hitch;

    @WinCCVari("MAG_ReturnPump21_Hitch_1.alarm")
    private Integer magReturnPump21Hitch;

    @WinCCVari("MAG_ReturnPump22_Hitch_1.alarm")
    private Integer magReturnPump22Hitch;

    @WinCCVari("MAG_Shear1_Hitch_1.alarm")
    private Integer magShear1Hitch;

    @WinCCVari("MAG_Shear2_Hitch_1.alarm")
    private Integer magShear2Hitch;

    @WinCCVari("MAG_System_SV1_Hitch_1.alarm")
    private Integer magSystemSV1Hitch1;

    @WinCCVari("MAG_System_SV1_Hitch_2.alarm")
    private Integer magSystemSV1Hitch2;

    @WinCCVari("MAG_System_SV2_Hitch_1.alarm")
    private Integer magSystemSV2Hitch1;

    @WinCCVari("MAG_System_SV2_Hitch_2.alarm")
    private Integer magSystemSV2Hitch2;

    @WinCCVari("MAG_SludgeTank_Hitch_1.alarm")
    private Integer magSludgeTankHitch1;

    @WinCCVari("WPS_LiquidProtect")
    private Integer wpsLiquidProtect;

    @WinCCVari("CSD_System.SystemError")
    private Integer csdSystem;

    @WinCCVari("MAG_System.SystemError")
    private Integer magSystem;

    @WinCCVari("NaAc1_System.SystemError")
    private Integer naac1System;

    @WinCCVari("NaAc2_System.SystemError")
    private Integer naac2System;

    @WinCCVari("NaCIO_System.SystemError")
    private Integer nacioSystem;

}
