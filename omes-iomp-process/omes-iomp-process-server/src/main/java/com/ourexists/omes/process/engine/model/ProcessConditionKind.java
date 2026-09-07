package com.ourexists.omes.process.engine.model;

import org.springframework.util.StringUtils;

public enum ProcessConditionKind {

    NONE,
    TIME,
    EVENT,
    /** 完成动作：执行结束后自动进入下一组合 */
    AUTO_NEXT,
    /** 完成动作：等待人工确认 */
    MANUAL_CONFIRM;

    public static ProcessConditionKind fromText(String text) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException("条件 kind 不能为空");
        }
        return ProcessConditionKind.valueOf(text.trim().toUpperCase());
    }

    public boolean isCompleteActionKind() {
        return this == AUTO_NEXT || this == MANUAL_CONFIRM;
    }

    public boolean isDriveOrExceptionKind() {
        return this == NONE || this == TIME || this == EVENT;
    }
}
