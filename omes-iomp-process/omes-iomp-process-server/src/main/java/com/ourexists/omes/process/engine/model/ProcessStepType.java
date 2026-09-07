package com.ourexists.omes.process.engine.model;

import org.springframework.util.StringUtils;

/**
 * 工艺步骤类型（与工序脚本 JSON 中 {@code type} 对应）。
 */
public enum ProcessStepType {

    RAMP,
    PID_CONTROL,
    /** 工序单元：含多组驱动组合 */
    COMBINATION;

    public static ProcessStepType fromText(String text) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException("工序步骤 type 不能为空");
        }
        return ProcessStepType.valueOf(text.trim().toUpperCase());
    }
}
