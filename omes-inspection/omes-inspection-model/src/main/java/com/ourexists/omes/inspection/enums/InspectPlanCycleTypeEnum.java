/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.enums;

import lombok.Getter;

/**
 * 巡检计划周期类型
 */
@Getter
public enum InspectPlanCycleTypeEnum {
    DAILY(1, "每日"),
    WEEKLY(2, "每周"),
    MONTHLY(3, "每月");

    private final int code;
    private final String desc;

    InspectPlanCycleTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static InspectPlanCycleTypeEnum valueOf(int code) {
        for (InspectPlanCycleTypeEnum e : values()) {
            if (e.code == code) return e;
        }
        return DAILY;
    }
}
