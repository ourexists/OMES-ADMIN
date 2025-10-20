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
@TableName("t_wincc_mag_dev")
public class WinCCMagDev extends EraEntity {

    private Boolean magBackupCircuit;
    private Boolean magCharge1;
    private Boolean magCharge2;
    private Boolean magFlocculation1;
    private Boolean magFlocculation2;
    private Boolean magMixer1;
    private Boolean magMixer2;
    private Boolean magResidualPump1;
    private Boolean magResidualPump2;
    private Boolean magReturnPump11;
    private Boolean magReturnPump12;
    private Boolean magReturnPump21;
    private Boolean magReturnPump22;
    private Boolean magScraper1;
    private Boolean magScraper2;
    private Boolean magSeparator1;
    private Boolean magSeparator2;
    private Boolean magSewagePump;
    private Boolean magShear1;
    private Boolean magShear2;
    private Boolean magSludgeConveyingPump01;
    private Boolean magSludgeConveyingPump02;
    private Date startTime;
    private Date endTime;
    private Date execTime;

    public static  WinCCMagDevDto covert( WinCCMagDev source) {
        if (source == null) {
            return null;
        }
         WinCCMagDevDto target = new  WinCCMagDevDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List< WinCCMagDevDto> covert(List< WinCCMagDev> sources) {
        List< WinCCMagDevDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static <T extends  WinCCMagDevDto>  WinCCMagDev wrap(T source) {
         WinCCMagDev target = new  WinCCMagDev();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends  WinCCMagDevDto> List< WinCCMagDev> wrap(List<T> sources) {
        List< WinCCMagDev> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }
}
