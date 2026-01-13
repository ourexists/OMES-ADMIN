/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class EquipRecordRunVo extends EquipRecordRunDto {

    private Date endTime;

    private BigDecimal powerEnd;

    private Long duration;

    private BigDecimal powerUse;

    public Long getDuration() {
        if (startTime == null || endTime == null) {
            return 0L;
        }
        return (endTime.getTime() - startTime.getTime()) / 60000;
    }

    public BigDecimal getPowerUse() {
        if (powerEnd == null || powerStart == null) {
            return BigDecimal.ZERO;
        }
        return powerStart.subtract(powerEnd);
    }
}
