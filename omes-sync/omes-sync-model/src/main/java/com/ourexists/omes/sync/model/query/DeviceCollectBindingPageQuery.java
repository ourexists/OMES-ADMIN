/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.sync.model.query;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@Schema(description = "设备采集绑定分页查询")
public class DeviceCollectBindingPageQuery extends PageQuery {

    @Schema(description = "连接ID")
    private String connectId;

    @Schema(description = "设备SN")
    private String equipSn;
}
