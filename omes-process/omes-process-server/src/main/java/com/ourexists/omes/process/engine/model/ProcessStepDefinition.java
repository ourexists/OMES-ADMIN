package com.ourexists.omes.process.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 单条工序步骤定义（与工序脚本 JSON 中单步结构对应，仅供参考的结构可演进）。
 */
@Data
@Schema(description = "工艺工序步骤定义")
public class ProcessStepDefinition {

    @Schema(description = "步骤类型：RAMP、PID_CONTROL、COMBINATION")
    private String type;

    @Schema(description = "驱动模式：TIME、CONTROL、UNIT")
    private String mode;

    @Schema(description = "时间驱动多段配置")
    private List<ProcessStepTimeSegment> segments;

    @Schema(description = "斜坡结束后设备 PID 控制（仅 RAMP+TIME）")
    private RampAfterControlSpec afterControl;

    @Schema(description = "工序脚本版本")
    private Integer scriptVersion;

    @Schema(description = "LiteFlow 脚本链分组键（工序 ID 或脚本摘要）")
    private String scriptChainKey;

    @Schema(description = "驱动组合（type=COMBINATION 时使用）")
    private List<ProcessStepCombination> combinations;

    @Schema(description = "事件/状态等待条件表达式")
    private String condition;

    @Schema(description = "关联设备编号（事件等待/斜坡/PID 控制绑定设备）")
    private String equipmentCode;

    @Schema(description = "设备属性/过程变量：temp 温度、pressure 压力等")
    private String variable;

    @Schema(description = "反馈控制目标值")
    private Double target;

    @Schema(description = "PID 比例系数")
    private Double kp;

    @Schema(description = "PID 积分系数")
    private Double ki;

    @Schema(description = "PID 微分系数")
    private Double kd;

    public ProcessStepType resolvedType() {
        return ProcessStepType.fromText(type);
    }

    public ProcessStepMode resolvedMode() {
        return ProcessStepMode.fromText(mode);
    }

    public List<ProcessStepTimeSegment> resolvedTimeSegments() {
        if (CollectionUtils.isEmpty(segments)) {
            throw new IllegalArgumentException("RAMP 步骤须配置 segments");
        }
        List<ProcessStepTimeSegment> valid = new ArrayList<>();
        for (ProcessStepTimeSegment segment : segments) {
            if (segment == null || segment.getTo() == null
                    || segment.getDuration() == null || segment.getDuration() <= 0) {
                throw new IllegalArgumentException("时间驱动段须包含有效的 to 与 duration");
            }
            valid.add(segment);
        }
        return valid;
    }
}
