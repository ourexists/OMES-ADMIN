package com.ourexists.mesedge.report.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_wincc_wps_dev")
public class WinCCWpsDev extends EraEntity {

    private Boolean wpsCirculatingCleaning1;
    private Boolean wpsSandPump1;
    private Boolean wpsBarScreen1;
    private Boolean wpsMixer1;
    private Boolean wpsSewagePump1;
    private Boolean wpsScrewConveyor1;
    private Boolean wpsCirculatingCleaning2;
    private Boolean wpsSandPump2;
    private Boolean wpsBarScreen2;
    private Boolean wpsMixer2;
    private Boolean wpsSewagePump2;
    private Boolean wpsScrewConveyor2;
    private Boolean wpsSewagePump3;
    private Boolean wpsSewagePump4;
    private Boolean wpsSewagePump5;
    private Boolean wpsSandSeparator;
    private Date startTime;
    private Date endTime;
    private Date execTime;

    public static WinCCWpsDevDto covert(WinCCWpsDev source) {
        if (source == null) {
            return null;
        }
        WinCCWpsDevDto target = new WinCCWpsDevDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<WinCCWpsDevDto> covert(List<WinCCWpsDev> sources) {
        List<WinCCWpsDevDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static <T extends WinCCWpsDevDto> WinCCWpsDev wrap(T source) {
        WinCCWpsDev target = new WinCCWpsDev();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends WinCCWpsDevDto> List<WinCCWpsDev> wrap(List<T> sources) {
        List<WinCCWpsDev> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }
}
