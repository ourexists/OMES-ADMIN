/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.mo.enums;

import lombok.Getter;

/**
 * 生产订单调整类型。
 * <pre>
 * P0: CANCEL_MO / CANCEL_MPS / RESCHEDULE / PRIORITY
 * P1: CHANGE_LINE / CHANGE_DEV / QTY_UP / QTY_DOWN
 * </pre>
 */
@Getter
public enum MoAdjustTypeEnum {

    CANCEL_MO,
    CANCEL_MPS,
    RESCHEDULE,
    PRIORITY,
    CHANGE_LINE,
    CHANGE_DEV,
    QTY_UP,
    QTY_DOWN;

    public static MoAdjustTypeEnum of(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        for (MoAdjustTypeEnum value : values()) {
            if (value.name().equalsIgnoreCase(name.trim())) {
                return value;
            }
        }
        return null;
    }
}
