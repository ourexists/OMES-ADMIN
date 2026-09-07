/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.mo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;

@Schema(description = "生产订单调整命令")
@Getter
@Setter
@Accessors(chain = true)
public class MoAdjustCommand {

    @NotBlank
    @Schema(description = "生产订单编号 t_mo.self_code")
    private String moCode;

    @NotBlank
    @Schema(description = "调整类型：CANCEL_MO/CANCEL_MPS/RESCHEDULE/PRIORITY/CHANGE_LINE/CHANGE_DEV/QTY_UP/QTY_DOWN")
    private String adjustType;

    @Schema(description = "类型载荷")
    private Map<String, Object> payload;

    @Schema(description = "来源：UI / MES / SYSTEM，默认 UI")
    private String source;

    @NotBlank
    @Schema(description = "幂等键，写入 t_mo_adjust_log.request_id UK")
    private String requestId;

    @Schema(description = "操作人；force=true 时必填")
    private String operator;

    @Schema(description = "强制：允许中止 EXECING（需同时提供 operator）。CHANGE_LINE 对 EXECING 仍拒绝")
    private Boolean force;
}
