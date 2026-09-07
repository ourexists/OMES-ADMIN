/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.mo.enums;

import lombok.Getter;

@Getter
public enum MoAdjustSourceEnum {

    UI,
    MES,
    SYSTEM;

    public static MoAdjustSourceEnum of(String name) {
        if (name == null || name.isBlank()) {
            return UI;
        }
        for (MoAdjustSourceEnum value : values()) {
            if (value.name().equalsIgnoreCase(name.trim())) {
                return value;
            }
        }
        return UI;
    }
}
