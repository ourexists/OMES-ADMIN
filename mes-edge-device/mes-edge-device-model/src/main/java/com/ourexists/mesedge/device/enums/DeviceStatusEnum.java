package com.ourexists.mesedge.device.enums;

import lombok.Getter;

@Getter
public enum DeviceStatusEnum {

    enable(0, "启用"),
    disable(1, "禁用");
    private Integer code;
    private String desc;

    DeviceStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DeviceStatusEnum valueof(Integer code) {
        if (code != null) {
            for (DeviceStatusEnum value : DeviceStatusEnum.values()) {
                if (value.code.equals(code)) {
                    return value;
                }
            }
        }
        return DeviceStatusEnum.enable;
    }
}
