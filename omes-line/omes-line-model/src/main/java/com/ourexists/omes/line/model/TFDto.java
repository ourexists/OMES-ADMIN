/*

 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists

 */



package com.ourexists.omes.line.model;



import com.ourexists.era.framework.core.model.dto.BaseDto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotEmpty;

import lombok.Getter;

import lombok.Setter;

import lombok.experimental.Accessors;



@Getter

@Setter

@Accessors(chain = true)

public class TFDto extends BaseDto {



    @Schema(hidden = true)

    private String id;



    @Schema(description = "编号")

    @NotEmpty(message = "${valid.selfcode.empty}")

    private String selfCode;



    @Schema(description = "名称")

    @NotEmpty(message = "${valid.name.empty}")

    private String name;



    @Schema(description = "工序号")

    private Integer stepNo;



    @Schema(description = "工序内容")

    private String stepContent;



    @Schema(description = "关联设备")

    private java.util.List<TfEquipmentRef> equipments;



    @Schema(description = "关联工装")

    private java.util.List<TfToolingRef> toolings;



    @Schema(description = "产线id", hidden = true)

    private String lineId;



    @Schema(description = "工序执行脚本 JSON（流程图/规则引擎）")

    private String stepScript;



    @Schema(description = "流程引擎编译配置 JSON（保存时生成）")

    private String stepEngineConfig;

}

