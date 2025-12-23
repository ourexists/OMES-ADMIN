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
public class EquipRunRecordDto {

    private String id;

    private String sn;

    private Date runStartTime;

    private Date runEndTime;

    private BigDecimal powerStart;

    private BigDecimal powerEnd;
}
