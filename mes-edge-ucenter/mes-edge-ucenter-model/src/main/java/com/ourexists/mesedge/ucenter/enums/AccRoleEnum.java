/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.enums;

/**
 * @author pengcheng
 * @date 2022/4/13 18:00
 * @since 1.0.0
 */
public enum AccRoleEnum {
    /**
     * 普通角色
     */
    COMMON,

    /**
     * 管理员
     */
    ADMIN;

    public static AccRoleEnum valueOfName(String name) {
        for (AccRoleEnum value : AccRoleEnum.values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return AccRoleEnum.COMMON;
    }
}
