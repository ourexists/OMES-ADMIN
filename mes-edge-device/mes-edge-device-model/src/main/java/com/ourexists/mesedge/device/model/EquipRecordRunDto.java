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
public class EquipRecordRunDto {

    protected String id;

    protected String sn;

    protected Date startTime;

    protected Integer state;

    protected BigDecimal powerStart;

    protected String tenantId;
}
