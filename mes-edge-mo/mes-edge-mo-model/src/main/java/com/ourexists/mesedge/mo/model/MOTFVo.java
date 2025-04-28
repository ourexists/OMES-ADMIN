/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class MOTFVo extends MOTFDto {

    private String createdBy;

    private String createdId;

    private Date createdTime;

    private String updatedBy;

    private String updatedId;

    private Date updatedTime;
}
