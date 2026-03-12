/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class EquipRecordAlarmDto {

    protected String id;

    protected String sn;

    protected Date startTime;

    protected Integer state;

    protected String tenantId;

    protected String reason;

    /** 报警等级: 0=轻微, 1=一般, 2=严重, 3=故障，见 AlarmLevelEnum，为空时按一般处理 */
    protected Integer level;
}
