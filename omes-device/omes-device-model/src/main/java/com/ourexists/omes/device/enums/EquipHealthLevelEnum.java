/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.enums;

import lombok.Getter;

/**
 * 设备健康等级：根据健康得分划分
 */
@Getter
public enum EquipHealthLevelEnum {

    /** 健康 */
    healthy(0, "健康"),
    /** 关注 */
    attention(1, "关注"),
    /** 预警 */
    warning(2, "预警"),
    /** 故障 */
    fault(3, "故障");

    private final Integer code;
    private final String desc;

    EquipHealthLevelEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据得分与模板阈值计算健康等级
     */
    public static EquipHealthLevelEnum fromScore(int score,
                                                 int healthyThreshold,
                                                 int attentionThreshold,
                                                 int warningThreshold) {
        if (score >= healthyThreshold) return healthy;
        if (score >= attentionThreshold) return attention;
        if (score >= warningThreshold) return warning;
        return fault;
    }

    public static EquipHealthLevelEnum valueOf(Integer code) {
        if (code != null) {
            for (EquipHealthLevelEnum value : EquipHealthLevelEnum.values()) {
                if (value.code.equals(code)) {
                    return value;
                }
            }
        }
        return attention;
    }
}
