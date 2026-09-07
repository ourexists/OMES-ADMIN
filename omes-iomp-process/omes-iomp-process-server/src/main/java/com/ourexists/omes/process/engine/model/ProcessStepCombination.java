package com.ourexists.omes.process.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 工序内一组驱动组合：主链上驱动 / 动作 / 完成的运行顺序由 {@link #phaseOrder} 决定（异常条件并行监测）。
 */
@Data
@Schema(description = "工序驱动组合")
public class ProcessStepCombination {

    @Schema(description = "组合名称")
    private String name;

    @Schema(description = "段内阶段运行顺序，如 [DRIVE,ACTION,COMPLETE] 或 [ACTION,DRIVE,COMPLETE]")
    private List<ProcessSegmentPhase> phaseOrder;

    @Schema(description = "驱动条件：满足后进入执行动作")
    private ProcessConditionSpec drive;

    @Schema(description = "执行动作")
    private ProcessStepDefinition action;

    @Schema(description = "完成动作：AUTO_NEXT 自动下一工序、MANUAL_CONFIRM 等待人工确认")
    private ProcessConditionSpec complete;

    @Schema(description = "异常条件：满足则中止本工序")
    private ProcessConditionSpec exception;

    @Schema(description = "LiteFlow 链 ID（编译期注册）")
    private String chainId;

    @Schema(description = "LiteFlow EL（可热刷新）")
    private String liteflowEl;
}
