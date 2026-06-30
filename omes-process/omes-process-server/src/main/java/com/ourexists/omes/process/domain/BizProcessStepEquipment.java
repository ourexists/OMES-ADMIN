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
@TableName("m_biz_process_step_equipment")
@Schema(description = "工艺工序关联设备")
public class BizProcessStepEquipment extends EraEntity {

    @Schema(description = "工序ID")
    private String stepId;

    @Schema(description = "设备编号")
    private String equipmentCode;

    @Schema(description = "设备名称")
    private String equipmentName;
}
