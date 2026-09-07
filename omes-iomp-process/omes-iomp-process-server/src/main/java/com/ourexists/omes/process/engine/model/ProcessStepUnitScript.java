package com.ourexists.omes.process.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 单条工序的完整执行脚本（存于 step_script）。
 */
@Data
@Schema(description = "工序单元脚本")
public class ProcessStepUnitScript {

    @Schema(description = "脚本版本，当前为 2")
    private Integer version = 2;

    @Schema(description = "驱动组合列表")
    private List<ProcessStepCombination> combinations = new ArrayList<>();
}
