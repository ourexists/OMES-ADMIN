package com.ourexists.mesedge.message.enums;

import lombok.Getter;

public enum NotifyStatusEnum {

    READY(0),
    PROGRESS(1),
    COMPLETED(2);

    @Getter
    private final Integer code;

    NotifyStatusEnum(int code) {
        this.code = code;
    }

    public static NotifyStatusEnum valueOf(Integer code) {
        for (NotifyStatusEnum value : NotifyStatusEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return NotifyStatusEnum.READY;
    }
}
