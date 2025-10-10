package com.ourexists.mesedge.sync.enums;

import lombok.Getter;

public enum ConnectValidTypeEnum {

    NONE(0),
    BASIC(1);

    @Getter
    private final Integer code;

    ConnectValidTypeEnum(Integer code) {
        this.code = code;
    }

    public static ConnectValidTypeEnum valueOf(Integer code) {
        for (ConnectValidTypeEnum value : ConnectValidTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return ConnectValidTypeEnum.NONE;
    }
}
