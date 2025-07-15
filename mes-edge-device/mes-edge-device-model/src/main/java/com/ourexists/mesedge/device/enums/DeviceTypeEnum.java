package com.ourexists.mesedge.device.enums;

import lombok.Getter;

@Getter
public enum DeviceTypeEnum {

    cang(0, "仓"),
    cheng(1, "秤"),
    gong(2, "工段");
    private Integer code;
    private String desc;

    DeviceTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DeviceTypeEnum valueof(Integer code) {
        if (code != null) {
            for (DeviceTypeEnum value : DeviceTypeEnum.values()) {
                if (value.code.equals(code)) {
                    return value;
                }
            }
        }
        return DeviceTypeEnum.cang;
    }
}
