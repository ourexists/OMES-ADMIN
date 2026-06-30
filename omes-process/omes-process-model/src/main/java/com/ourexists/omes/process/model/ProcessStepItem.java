package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "工序项")
public class ProcessStepItem {

    @Schema(description = "工序 ID（查询返回；保存时忽略）")
    private String id;

    @Schema(description = "工序号")
    private Integer stepNo;

    @Schema(description = "工序编码（Route Step）")
    private String stepCode;

    @Schema(description = "工序名称")
    @NotBlank(message = "工序名称不能为空")
    private String stepName;

    @Schema(description = "工序内容（工艺卡片文本）")
    private String stepContent;

    @Schema(description = "工序执行脚本 JSON，如 {\"type\":\"RAMP\",\"mode\":\"TIME\",\"to\":80,\"duration\":60}")
    private String stepScript;

    @Schema(description = "工序动态参数 JSON，如 {\"segments\":[{\"to\":80,\"duration\":60}]}")
    private String params;

    @Schema(description = "流程引擎编译配置（只读；process.engine.config-source=database 时保存写入）")
    private String stepEngineConfig;

    @Schema(description = "WIP/排产配置（独立表维护，仅详情查询返回）")
    private ProcessStepWipItem wip;

    @Schema(description = "关联设备")
    @Valid
    private List<ProcessEquipmentRef> equipments = new ArrayList<>();

    @Schema(description = "关联工装")
    @Valid
    private List<ProcessToolingRef> toolings = new ArrayList<>();
}
