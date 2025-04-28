/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.model;

import com.ourexists.mesedge.mps.enums.QAResultEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Schema
@Accessors(chain = true)
public class QAVo extends QADto {

    private String createdBy;

    private String createdId;

    private Date createdTime;

    private String updatedBy;

    private String updatedId;

    private Date updatedTime;

    @Schema(description = "质检结果")
    protected String resultDesc;
//
//    private MPSVo mps;
//
//    private MPSTFVo mpstf;

    public String getResultDesc() {
        return QAResultEnum.valueOf(result).getName();
    }
}
