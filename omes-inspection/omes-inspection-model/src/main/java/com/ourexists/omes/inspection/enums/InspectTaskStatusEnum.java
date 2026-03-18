/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.enums;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 巡检任务执行状态（后端统一枚举）
 */
@Getter
public enum InspectTaskStatusEnum {
    PENDING(0, "待执行"),
    IN_PROGRESS(1, "执行中"),
    COMPLETED(2, "已完成"),
    OVERDUE(3, "已逾期");

    private final int code;
    private final String desc;

    InspectTaskStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static InspectTaskStatusEnum valueOf(int code) {
        for (InspectTaskStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return PENDING;
    }

    /** 供前端下拉等使用：code -> desc */
    public static Map<Integer, String> toMap() {
        Map<Integer, String> map = new LinkedHashMap<>();
        for (InspectTaskStatusEnum e : values()) {
            map.put(e.getCode(), e.getDesc());
        }
        return map;
    }
}
