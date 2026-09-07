/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.omes.device.model.EquipHealthIndicatorDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_equip_health_indicator")
public class EquipHealthIndicator extends EraEntity {

    private String equipId;
    private String sn;

    private Date statTime;

    private Date periodStart;

    private Date periodEnd;

    private Integer score;

    private Integer healthLevel;

    private Integer alarmCount;

    private Long alarmDurationMinutes;

    private Long runDurationMinutes;

    private Long onlineDurationMinutes;

    private Long periodMinutes;
    
    private String templateId;

    public static EquipHealthIndicatorDto toDto(EquipHealthIndicator source) {
        if (source == null) return null;
        EquipHealthIndicatorDto dto = new EquipHealthIndicatorDto();
        BeanUtils.copyProperties(source, dto);
        return dto;
    }

    public static EquipHealthIndicator fromDto(EquipHealthIndicatorDto dto) {
        if (dto == null) return null;
        EquipHealthIndicator entity = new EquipHealthIndicator();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
