/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ec.model;

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
public class EcRecordQuery {

    private List<String> attrIds;

    private Date startTime;

    private Date endTime;
}
