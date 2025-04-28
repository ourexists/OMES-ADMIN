/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mo.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class MODetailDto extends BaseDto {

    private String id;

    @Schema(description = "物料id")
    private String matId;

    @Schema(description = "清单编号")
    private String mcode;

    @Schema(description = "物料名称")
    private String matName;

    @Schema(description = "物料编号")
    private String matCode;

    @Schema(description = "物料数量")
    private BigDecimal matNum;

    @Schema(description = "物料占比")
    private BigDecimal matScale;

    @Schema(description = "优先级")
    private Integer priority;

}
