/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.line.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class LineDto extends BaseDto {

    protected String id;

    @NotBlank(message = "${valid.selfcode.empty}")
    protected String selfCode;

    @NotBlank(message = "${valid.name.empty}")
    protected String name;

    @Schema(description = "加工对象编号")
    protected String productCode;

    @Schema(description = "加工对象名称")
    protected String productName;

    @Schema(description = "原材料编号")
    protected String materialCode;

    @Schema(description = "原材料名称")
    protected String materialName;

    @Schema(description = "版本号")
    protected String versionNo;

    protected BigDecimal throughput;

    protected Integer stepInterval;

    protected Integer type;

    @Schema(description = "数据同步时间")
    private Date syncTime;

    @Schema(description = "plc下载时间")
    private Date plcTime;

    @Schema(description = "映射内存")
    private Integer mapDb;

    @Schema(description = "映射数据偏移量")
    protected String mapOffset;
}
