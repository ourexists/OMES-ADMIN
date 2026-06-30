package com.ourexists.omes.process.engine.model;

import org.springframework.util.StringUtils;

/**
 * 工序步骤驱动模式：时间 / 事件 / 状态 / 反馈控制。
 */
public enum ProcessStepMode {

    TIME,
    CONTROL,
    /** 工序内多驱动组合顺序执行 */
    UNIT;

    public static ProcessStepMode fromText(String text) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException("工序步骤 mode 不能为空");
        }
        return ProcessStepMode.valueOf(text.trim().toUpperCase());
    }
}
