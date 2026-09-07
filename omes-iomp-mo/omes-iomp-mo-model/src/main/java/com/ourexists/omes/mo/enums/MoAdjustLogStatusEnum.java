/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.mo.enums;

import lombok.Getter;

@Getter
public enum MoAdjustLogStatusEnum {

    PENDING(0),
    SUCCESS(1),
    FAILED(2);

    private final Integer code;

    MoAdjustLogStatusEnum(Integer code) {
        this.code = code;
    }

    public static MoAdjustLogStatusEnum valueOf(Integer code) {
        for (MoAdjustLogStatusEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return PENDING;
    }
}
