/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.message.model.query;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Schema
public class MessagePageQuery extends PageQuery {

    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "所属平台")
    private String platform;

    @Schema(description = "用户id")
    private String accId;

    @Schema(description = "阅读状态")
    private Integer readStatus;

    @Schema(description = "创建的开始时间")
    private Date createdTimeStart;

    @Schema(description = "创建的结束时间")
    private Date createdTimeEnd;

}
