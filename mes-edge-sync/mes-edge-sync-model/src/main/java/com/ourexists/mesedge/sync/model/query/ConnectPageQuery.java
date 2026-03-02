/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.model.query;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Schema(description = "连接分页查询")
@Accessors(chain = true)
public class ConnectPageQuery extends PageQuery {

    @Schema(description = "协议")
    private String protocol;

    @Schema(description = "服务名")
    private String serverName;

    @Schema(description = "主机/IP")
    private String host;
}
