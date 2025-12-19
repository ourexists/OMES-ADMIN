package com.ourexists.mesedge.ucenter.enums;

import lombok.Getter;

public enum PermissionStrategyEnum {

    ENABLE_SHOW(0),
    ENABLE_NOTSHOW(1),
    NOT_ENABLE(2);

    @Getter
    private Integer code;

    PermissionStrategyEnum(Integer code) {
        this.code = code;
    }

    public static PermissionStrategyEnum valueof(Integer code) {
        if (code != null) {
            for (PermissionStrategyEnum value : PermissionStrategyEnum.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
        }
        return PermissionStrategyEnum.ENABLE_SHOW;
    }
}
