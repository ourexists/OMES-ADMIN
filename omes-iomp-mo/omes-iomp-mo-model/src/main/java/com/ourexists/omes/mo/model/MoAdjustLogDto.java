/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.mo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Schema(description = "生产订单调整审计日志")
@Getter
@Setter
@Accessors(chain = true)
public class MoAdjustLogDto {

    private String id;

    private String requestId;

    private String moCode;

    private String adjustType;

    private String source;

    private String beforeJson;

    private String afterJson;

    private String affectMpsIds;

    private Integer status;

    private String errMsg;

    private String operator;

    private Date createTime;
}
