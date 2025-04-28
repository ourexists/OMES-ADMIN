/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.line.model;

import com.ourexists.mesedge.line.enums.LineTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class LineVo extends LineDto {

    private String createdBy;

    private String createdId;

    private Date createdTime;

    private String updatedBy;

    private String updatedId;

    private Date updatedTime;

    private String typeDesc;

    private List<TFVo> tfs;

    public String getTypeDesc() {
        return LineTypeEnum.valueOf(type).getName();
    }
}
