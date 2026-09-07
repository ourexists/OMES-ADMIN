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
public class EquipRecordOnlineDto {

    protected String id;

    protected String sn;

    protected Date startTime;

    protected Integer state;

    protected String tenantId;

    /** 当前区间行的业务事件 ID（流侧生成，入库） */
    protected String eventId;

    /** 上一段未闭合行的 event_id；有值时优先按此更新 end_time */
    protected String prevEventId;
}
