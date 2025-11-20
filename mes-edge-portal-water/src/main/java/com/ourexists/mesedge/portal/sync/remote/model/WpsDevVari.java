package com.ourexists.mesedge.portal.sync.remote.model;

import com.ourexists.mesedge.portal.sync.remote.WinCCVari;
import com.ourexists.mesedge.report.model.WinCCWpsDevDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class WpsDevVari {

    @WinCCVari("1#提砂泵")
    private Integer wpsSandPump1;

    @WinCCVari("2#提砂泵")
    private Integer wpsSandPump2;

    @WinCCVari("1#格栅除污机")
    private Integer wpsBarScreen1;

    @WinCCVari("2#格栅除污机")
    private Integer wpsBarScreen2;

    @WinCCVari("1#沉砂池搅拌机")
    private Integer wpsMixer1;

    @WinCCVari("2#沉砂池搅拌机")
    private Integer wpsMixer2;

    @WinCCVari("1#潜水排污泵")
    private Integer wpsSewagePump1;

    @WinCCVari("2#潜水排污泵")
    private Integer wpsSewagePump2;

    @WinCCVari("3#潜水排污泵")
    private Integer wpsSewagePump3;

    @WinCCVari("4#潜水排污泵")
    private Integer wpsSewagePump4;

    @WinCCVari("5#潜水排污泵")
    private Integer wpsSewagePump5;

    @WinCCVari("1#螺旋输送机")
    private Integer wpsScrewConveyor1;

    @WinCCVari("2#螺旋输送机")
    private Integer wpsScrewConveyor2;

    @WinCCVari("WPS_xigeza1")
    private Integer wpsCirculatingCleaning1;

    @WinCCVari("WPS_xigeza2")
    private Integer wpsCirculatingCleaning2;

    @WinCCVari("砂分离器")
    private Integer wpsSandSeparator;

    @WinCCVari("WPS_chongxiPump")
    private Integer wpsChongxiPump;

    public static WinCCWpsDevDto covert(WpsDevVari source) {
        WinCCWpsDevDto target = new WinCCWpsDevDto();
        BeanUtils.copyProperties(source, target);
        target.setExecTime(new Date());
        return target;
    }
}
