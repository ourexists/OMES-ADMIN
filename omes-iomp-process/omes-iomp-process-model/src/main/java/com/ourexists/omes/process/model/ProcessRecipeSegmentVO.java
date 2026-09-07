package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "工艺配方斜坡段")
public class ProcessRecipeSegmentVO {

    @Schema(description = "目标值")
    private Double to;

    @Schema(description = "升温时长（秒）")
    private Integer duration;

    @Schema(description = "保持时长（秒）")
    private Integer holdDuration;
}
