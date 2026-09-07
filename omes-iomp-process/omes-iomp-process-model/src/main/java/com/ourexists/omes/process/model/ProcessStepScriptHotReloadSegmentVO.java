package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "热刷新后的单段 LiteFlow 链信息")
public class ProcessStepScriptHotReloadSegmentVO {

    @Schema(description = "段序号，从 0 开始")
    private int segmentIndex;

    @Schema(description = "段名称")
    private String name;

    @Schema(description = "LiteFlow 链 ID")
    private String chainId;

    @Schema(description = "LiteFlow EL")
    private String liteflowEl;

    @Schema(description = "驱动条件类型 NONE/TIME/EVENT")
    private String driveKind;

    @Schema(description = "完成动作 AUTO_NEXT/MANUAL_CONFIRM")
    private String completeKind;
}
