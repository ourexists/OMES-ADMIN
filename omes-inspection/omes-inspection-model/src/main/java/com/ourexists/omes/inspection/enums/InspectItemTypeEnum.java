/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.enums;

import lombok.Getter;

/**
 * 巡检项数据类型：选择、数值、是否
 */
@Getter
public enum InspectItemTypeEnum {
    SELECT(1, "选择"),
    NUMBER(2, "数值"),
    YES_NO(3, "是否");

    private final int code;
    private final String desc;

    InspectItemTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static InspectItemTypeEnum valueOf(int code) {
        for (InspectItemTypeEnum e : values()) {
            if (e.code == code) return e;
        }
        return SELECT;
    }
}
