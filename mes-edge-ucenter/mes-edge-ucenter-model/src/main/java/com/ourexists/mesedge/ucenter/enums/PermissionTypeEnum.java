package com.ourexists.mesedge.ucenter.enums;

import lombok.Getter;

public enum PermissionTypeEnum {

    MENU(0),
    BUTTON(1),
    OTHER(2);

    @Getter
    private Integer code;

    PermissionTypeEnum(Integer code) {
        this.code = code;
    }


    public static PermissionTypeEnum valueof(Integer code) {
        if (code != null) {
            for (PermissionTypeEnum value : PermissionTypeEnum.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
        }
        return PermissionTypeEnum.OTHER;
    }
}
