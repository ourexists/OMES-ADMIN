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
import java.util.List;


@Schema
@Getter
@Setter
@Accessors(chain = true)
public class EquipStateSnapshotCountQuery extends PageQuery {

    private Date startDate;

    private Date endDate;

    private Integer runState;

    private Integer alarmState;

    private Integer onlineState;

    private Integer countType;

    private String workshopCode;

    @Schema(description = "是否需要场景级联查询")
    private Boolean needWorkshopCascade = false;

    private List<String> workshopCodes;
}
