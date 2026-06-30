package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Schema(description = "工艺配方摘要")
public class ProcessRecipeVO {

    @Schema(description = "配方 ID")
    private String recipeId;

    @Schema(description = "配方名称")
    private String name;

    @Schema(description = "说明")
    private String description;

    @Schema(description = "默认设备编号")
    private String equipmentCode;

    @Schema(description = "过程量变量")
    private String variable;

    @Schema(description = "斜坡段数")
    private int segmentCount;

    @Schema(description = "恒温结束后是否关闭加热")
    private boolean shutdownEnabled;

    @Schema(description = "斜坡段明细")
    @Builder.Default
    private List<ProcessRecipeSegmentVO> segments = new ArrayList<>();
}
