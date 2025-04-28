/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import com.ourexists.mesedge.mat.enums.IngredientAttributeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class BOMDDto extends BaseDto {

    private String id;

    @Schema(description = "物料id")
    private String matId;

    @Schema(description = "物料名称")
    private String matName;

    @Schema(description = "物料编号")
    private String matCode;

    @Schema(description = "物料占比")
    private BigDecimal matScale;

    @Schema(description = "对应的主体编号")
    private String mcode;

    @Schema(description = "组份性质, 0=主料，1=预混料（添加剂）,2=回机料,3=油,4=水")
    private Integer attribute;

    private String attributeDesc;

    public String getAttributeDesc() {
        return IngredientAttributeEnum.valueOf(this.attribute).getName();
    }
}
