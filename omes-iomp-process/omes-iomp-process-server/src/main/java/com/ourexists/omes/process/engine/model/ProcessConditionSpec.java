package com.ourexists.omes.process.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 驱动/异常条件（NONE、TIME、EVENT）或完成动作（AUTO_NEXT、MANUAL_CONFIRM）规格。
 */
@Data
@Schema(description = "工序条件/完成动作规格")
public class ProcessConditionSpec {

    @Schema(description = "NONE|TIME|EVENT（驱动/异常）或 AUTO_NEXT|MANUAL_CONFIRM（完成动作）")
    private String kind;

    @Schema(description = "关联设备编号（事件条件）")
    private String equipmentCode;

    @Schema(description = "事件表达式（仅驱动/异常 EVENT）")
    private String condition;

    @Schema(description = "时间条件秒数（仅驱动/异常 TIME）")
    private Integer duration;

    @Schema(description = "时间条件多段（可选）")
    private List<ProcessStepTimeSegment> segments;

    @Schema(description = "Aviator 表达式（编译期生成，运行期求值）")
    private String aviatorExpression;

    public ProcessConditionKind resolvedKind() {
        return ProcessConditionKind.fromText(kind);
    }
}
