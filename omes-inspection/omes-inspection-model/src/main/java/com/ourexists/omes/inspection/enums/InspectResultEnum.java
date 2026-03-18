/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.enums;

import lombok.Getter;

/**
 * 巡检记录结果
 */
@Getter
public enum InspectResultEnum {
    NORMAL(0, "正常"),
    ABNORMAL(1, "异常");

    private final int code;
    private final String desc;

    InspectResultEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static InspectResultEnum valueOf(int code) {
        for (InspectResultEnum e : values()) {
            if (e.code == code) return e;
        }
        return NORMAL;
    }
}
