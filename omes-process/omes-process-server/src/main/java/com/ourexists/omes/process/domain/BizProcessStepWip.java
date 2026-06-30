package com.ourexists.omes.process.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@TableName("r_biz_process_step_wip")
@Schema(description = "工序 WIP/排产配置（全局按工序名关联，可跨工艺复用）")
public class BizProcessStepWip extends EraEntity {

    @Schema(description = "工序名称（全局唯一业务键）")
    private String stepName;

    @Schema(description = "是否产出 WIP")
    private Integer produceWipFlag;

    @Schema(description = "是否直送下一工序")
    private Integer directTransferFlag;

    @Schema(description = "WIP 类型")
    private String wipType;

    @Schema(description = "WIP 可存放时长（小时）")
    private java.math.BigDecimal wipHoldTimeHours;

    @Schema(description = "排产指定设备编码")
    private String scheduleDeviceCode;

    @Schema(description = "拉料目标工序名称")
    private String wipTriggerTargetStepName;
}
