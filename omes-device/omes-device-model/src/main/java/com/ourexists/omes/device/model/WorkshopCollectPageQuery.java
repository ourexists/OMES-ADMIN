/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.model;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;


@Schema
@Getter
@Setter
@Accessors(chain = true)
public class WorkshopCollectPageQuery extends PageQuery {

    @Schema(description = "场景id")
    private String workshopId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endDate;


}
