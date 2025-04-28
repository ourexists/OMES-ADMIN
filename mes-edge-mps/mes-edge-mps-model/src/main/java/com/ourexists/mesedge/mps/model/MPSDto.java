/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import com.ourexists.mesedge.mps.enums.MPSStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class MPSDto extends BaseDto {

    private String id;

    @Schema(description = "生产订单编号")
    @NotBlank(message = "${valid.selfcode.empty}")
    private String moCode;

    @Schema(description = "生产执行序列号")
    private Integer sequence = 0;

    @Schema(description = "执行时间")
    private Date execTime;

    @Schema(description = "执行产线")
    private String line;

    @Schema(description = "订单批次")
    private Integer batch;

    @Schema(description = "生产数量")
    private Integer num;

    @Schema(description = "生产重量")
    private BigDecimal weight;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "成分详情(根据条件查询返回。默认不返回)")
    private List<MPSDetailDto> details;

    @Schema(description = "工艺详情(根据条件查询返回。默认不返回)")
    private List<MPSTFVo> tfs;

    public String getStatusDesc() {
        return MPSStatusEnum.valueOf(this.status).getName();
    }


}
