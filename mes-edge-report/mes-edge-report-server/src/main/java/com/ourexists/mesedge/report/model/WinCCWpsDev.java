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

    private Integer wpsCirculatingCleaning1;
    private Integer wpsSandPump1;
    private Integer wpsBarScreen1;
    private Integer wpsMixer1;
    private Integer wpsSewagePump1;
    private Integer wpsScrewConveyor1;
    private Integer wpsCirculatingCleaning2;
    private Integer wpsSandPump2;
    private Integer wpsBarScreen2;
    private Integer wpsMixer2;
    private Integer wpsSewagePump2;
    private Integer wpsScrewConveyor2;
    private Integer wpsSewagePump3;
    private Integer wpsSewagePump4;
    private Integer wpsSewagePump5;
    private Integer wpsSandSeparator;
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
