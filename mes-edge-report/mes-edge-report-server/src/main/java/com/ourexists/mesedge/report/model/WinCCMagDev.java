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

    private Integer magBackupCircuit;
    private Integer magCharge1;
    private Integer magCharge2;
    private Integer magFlocculation1;
    private Integer magFlocculation2;
    private Integer magMixer1;
    private Integer magMixer2;
    private Integer magResidualPump1;
    private Integer magResidualPump2;
    private Integer magReturnPump11;
    private Integer magReturnPump12;
    private Integer magReturnPump21;
    private Integer magReturnPump22;
    private Integer magScraper1;
    private Integer magScraper2;
    private Integer magSeparator1;
    private Integer magSeparator2;
    private Integer magSewagePump;
    private Integer magShear1;
    private Integer magShear2;
    private Integer magSludgeConveyingPump01;
    private Integer magSludgeConveyingPump02;
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
