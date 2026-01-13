/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class EquipRecordAlarmVo extends EquipRecordAlarmDto {

    private Date endTime;

    private Long duration;

    public Long getDuration() {
        if (startTime == null || endTime == null) {
            return 0L;
        }
        return (endTime.getTime() - startTime.getTime()) / 60000;
    }
}
