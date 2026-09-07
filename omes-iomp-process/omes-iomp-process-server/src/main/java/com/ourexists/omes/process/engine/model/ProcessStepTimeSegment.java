package com.ourexists.omes.process.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 时间驱动单段：从上一段终点起，duration 秒内线性升至 to，可选 holdDuration 秒保持 to。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "时间驱动段")
public class ProcessStepTimeSegment {

    @Schema(description = "本段终点目标值（过程量，如温度、压力、水位等）")
    private Double to;

    @Schema(description = "本段运行时长（秒），自上一段终点线性升至 to")
    private Integer duration;

    @Schema(description = "到达 to 后保持时长（秒），null 或 0 表示不保持")
    private Integer holdDuration;
}
