/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import com.ourexists.mesedge.mat.enums.BOMTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class BOMDto extends BaseDto {

    private String id;

    @Schema(description = "配方名称")
    @NotBlank(message = "${valid.name.empty}")
    private String name;

    @Schema(description = "配方编号")
    @NotBlank(message = "${valid.selfcode.empty}")
    private String selfCode;

    @Schema(description = "所属分类编号")
    @NotBlank(message = "${valid.classifycode.empty}")
    private String classifyCode;

    @Schema(description = "类别")
    private Integer type;

    @Schema(description = "类别描述")
    private String typeDesc;

    @Schema(description = "成分详情(根据条件查询返回。默认不返回)")
    private List<BOMDDto> details;

    public String getTypeDesc() {
        return BOMTypeEnum.valueOf(this.type).getName();
    }
}
