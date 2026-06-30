package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "工序 WIP/排产配置")
public class ProcessStepWipItem {

    @Schema(description = "是否产出 WIP")
    private Boolean produceWipFlag;

    @Schema(description = "是否直送下一工序")
    private Boolean directTransferFlag;

    @Schema(description = "WIP 类型")
    private String wipType;

    @Schema(description = "WIP 可存放时长（小时）")
    private java.math.BigDecimal wipHoldTimeHours;

    @Schema(description = "排产指定设备编码")
    private String scheduleDeviceCode;

    @Schema(description = "拉料目标工序名称")
    private String wipTriggerTargetStepName;
}
