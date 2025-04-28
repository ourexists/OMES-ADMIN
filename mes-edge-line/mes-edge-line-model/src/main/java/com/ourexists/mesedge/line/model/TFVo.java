/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.line.model;

import com.ourexists.mesedge.line.enums.TFTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class TFVo extends TFDto {

    private String createdBy;

    private String createdId;

    private Date createdTime;

    private String updatedBy;

    private String updatedId;

    private Date updatedTime;

    @Schema(description = "类型描述")
    private String typeDesc;

    public String getTypeDesc() {
        return TFTypeEnum.valueOf(type).getName();
    }
}
