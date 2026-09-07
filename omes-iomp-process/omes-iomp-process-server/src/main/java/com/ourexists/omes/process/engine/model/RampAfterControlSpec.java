package com.ourexists.omes.process.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 斜坡全部段完成后的设备闭环控制（PID），须绑定设备与属性。
 */
@Data
@Schema(description = "斜坡后设备控制")
public class RampAfterControlSpec {

    @Schema(description = "是否在斜坡结束后执行控制")
    private Boolean enabled;

    @Schema(description = "关联设备编号")
    private String equipmentCode;

    @Schema(description = "设备属性/过程变量：temp 温度、pressure 压力等")
    private String variable;

    @Schema(description = "控制目标值")
    private Double target;

    @Schema(description = "PID 比例系数")
    private Double kp;

    @Schema(description = "PID 积分系数")
    private Double ki;

    @Schema(description = "PID 微分系数")
    private Double kd;
}
