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
@TableName("t_wincc_dosing_dev")
public class WinCCDosingDev extends EraEntity {

    private Integer pamBlender1;
    private Integer pamBlender2;
    private Integer pamDispensing;
    private Integer pamHeater;
    private Integer pamScrewPump1;
    private Integer pamScrewPump2;
    private Integer pamScrewPump3;
    private Integer pamVibrator;
    private Integer pamWaterInlet;
    private Integer pfsDosingPumpA;
    private Integer pfsDosingPumpB;
    private Integer pfsDosingPumpC;
    private Integer pfsUnloadingPump;
    private Integer naac1DosingPumpA;
    private Integer naac1DosingPumpB;
    private Integer naac1DosingPumpC;
    private Integer naac1UnloadingPump;
    private Integer naac2DosingPumpA;
    private Integer naac2DosingPumpB;
    private Date startTime;
    private Date endTime;
    private Date execTime;

    public static WinCCDosingDevDto covert(WinCCDosingDev source) {
        if (source == null) {
            return null;
        }
        WinCCDosingDevDto target = new WinCCDosingDevDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<WinCCDosingDevDto> covert(List<WinCCDosingDev> sources) {
        List<WinCCDosingDevDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static <T extends WinCCDosingDevDto> WinCCDosingDev wrap(T source) {
        WinCCDosingDev target = new WinCCDosingDev();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends WinCCDosingDevDto> List<WinCCDosingDev> wrap(List<T> sources) {
        List<WinCCDosingDev> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }
}
