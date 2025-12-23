/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.model;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;


@Schema
@Getter
@Setter
@Accessors(chain = true)
public class EquipRunRecordPageQuery extends PageQuery {

    private Date runStartTimeStart;

    private Date runEndTimeEnd;
}
