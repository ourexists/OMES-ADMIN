package com.ourexists.mesedge.portal.sync.remote.model;

import com.ourexists.mesedge.portal.sync.remote.WinCCVari;
import com.ourexists.mesedge.report.model.WinCCMagDevDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class MagDevVari {

    @WinCCVari("MAG_BackupCircuit")
    private Integer magBackupCircuit;

    @WinCCVari("MAG_Charge1")
    private Integer magCharge1;

    @WinCCVari("MAG_Charge2")
    private Integer magCharge2;

    @WinCCVari("MAG_Flocculation1")
    private Integer magFlocculation1;

    @WinCCVari("MAG_Flocculation2")
    private Integer magFlocculation2;

    @WinCCVari("MAG_Mixer1")
    private Integer magMixer1;

    @WinCCVari("MAG_Mixer2")
    private Integer magMixer2;

    @WinCCVari("MAG_ResidualPump1")
    private Integer magResidualPump1;

    @WinCCVari("MAG_ResidualPump2")
    private Integer magResidualPump2;

    @WinCCVari("MAG_ReturnPump11")
    private Integer magReturnPump11;

    @WinCCVari("MAG_ReturnPump12")
    private Integer magReturnPump12;

    @WinCCVari("MAG_ReturnPump21")
    private Integer magReturnPump21;

    @WinCCVari("MAG_ReturnPump22")
    private Integer magReturnPump22;

    @WinCCVari("MAG_Scraper1")
    private Integer magScraper1;

    @WinCCVari("MAG_Scraper2")
    private Integer magScraper2;

    @WinCCVari("MAG_Separator1")
    private Integer magSeparator1;

    @WinCCVari("MAG_Separator2")
    private Integer magSeparator2;

    @WinCCVari("MAG_SewagePump")
    private Integer magSewagePump;

    @WinCCVari("MAG_Shear1")
    private Integer magShear1;

    @WinCCVari("MAG_Shear2")
    private Integer magShear2;

    @WinCCVari("MAG_SludgeConveyingPump01")
    private Integer magSludgeConveyingPump01;

    @WinCCVari("MAG_SludgeConveyingPump02")
    private Integer magSludgeConveyingPump02;

    public static WinCCMagDevDto covert(MagDevVari source) {
        WinCCMagDevDto target = new WinCCMagDevDto();
        BeanUtils.copyProperties(source, target);
        target.setExecTime(new Date());
        return target;
    }
}
