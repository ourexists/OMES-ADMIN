/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.enums;

import lombok.Getter;

/**
 * 租户状态
 *
 * @author pengcheng
 * @date 2022/4/2 16:26
 * @since 1.0.0
 */
public enum AccStatusEnum {
    /**
     *
     */
    COMMON(0, "正常"),
    FROZEN(1, "冻结"),
    INVALID(2, "失效");

    @Getter
    private final Integer code;

    @Getter
    private final String desc;

    AccStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AccStatusEnum valueof(Integer code){
        for (AccStatusEnum value : AccStatusEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return AccStatusEnum.COMMON;
    }
}
