/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.message.model.query;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

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

}
