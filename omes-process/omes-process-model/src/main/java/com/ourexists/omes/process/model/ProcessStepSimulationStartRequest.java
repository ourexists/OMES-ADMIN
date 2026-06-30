package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Schema(description = "启动工艺工序仿真")
public class ProcessStepSimulationStartRequest {

    @Schema(description = "工艺ID（与 scriptJson 二选一）")
    private String processId;

    @Schema(description = "整段工序脚本 JSON（与 processId、recipeId 三选一）")
    private String scriptJson;

    @Schema(description = "工艺配方 ID（classpath process-recipes/*.yml，与 processId、scriptJson 三选一）")
    private String recipeId;

    @Schema(description = "覆盖配方默认设备编号（仅 recipeId 模式有效）")
    private String equipmentCode;

    @Schema(description = "仿真初始温度")
    private Double initialTemperature;

    @Schema(description = "仿真初始离散状态，如 DOOR_CLOSED=true")
    private Map<String, Boolean> initialStates = new LinkedHashMap<>();
}
