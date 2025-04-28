/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mo.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import com.ourexists.mesedge.mo.enums.MOStatusEnum;
import com.ourexists.mesedge.mo.enums.MoSourceEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class MODto extends BaseDto {

    private String id;

    private String tenantId;

    @Schema(description = "产品id")
    private String productId;

    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "产品编号")
    private String productCode;

    @Schema(description = "生产批次数量")
    private Integer num;

    @Schema(description = "剩余待生产数量")
    private Integer surplus;

    @Schema(description = "单批次生产重量")
    private BigDecimal weight;

    @Schema(description = "编号")
    private String selfCode;

    @Schema(description = "产线工艺编号")
    private String lineCode;

    @Schema(description = "清单状态")
    private Integer status;

    @Schema(description = "清单状态")
    private String statusDesc;

    @Schema(description = "预期执行时间")
    private Date execTime;

    @Schema(description = "来源")
    private Integer source;

    @Schema(description = "来源")
    private String sourceDesc;

    @Schema(description = "来源Id")
    private String sourceId;

    @Schema(description = "清单详情（只有设置对应条件才会返回）")
    private List<MODetailDto> detailDtoList;

    @Schema(description = "工艺详情（只有设置对应条件才会返回）")
    private List<MOTFDto> tfDtoList;

    private String createdBy;

    private String createdId;

    private Date createdTime;

    public String getStatusDesc() {
        return MOStatusEnum.valueOf(this.status).getName();
    }

    public String getSourceDesc() {
        return MoSourceEnum.valueOf(this.source).getName();
    }
}
