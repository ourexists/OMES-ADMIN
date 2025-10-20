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

    private Boolean pamBlender1;
    private Boolean pamBlender2;
    private Boolean pamDispensing;
    private Boolean pamHeater;
    private Boolean pamScrewPump1;
    private Boolean pamScrewPump2;
    private Boolean pamScrewPump3;
    private Boolean pamVibrator;
    private Boolean pamWaterInlet;
    private Boolean pfsDosingPumpA;
    private Boolean pfsDosingPumpB;
    private Boolean pfsDosingPumpC;
    private Boolean pfsUnloadingPump;
    private Boolean naac1DosingPumpA;
    private Boolean naac1DosingPumpB;
    private Boolean naac1DosingPumpC;
    private Boolean naac1UnloadingPump;
    private Boolean naac2DosingPumpA;
    private Boolean naac2DosingPumpB;
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
