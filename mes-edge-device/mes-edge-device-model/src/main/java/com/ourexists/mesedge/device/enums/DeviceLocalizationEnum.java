package com.ourexists.mesedge.device.enums;

import lombok.Getter;

@Getter
public enum DeviceLocalizationEnum {

    NO(0, "无"),
    yuanliao(1, "原料仓"),
    zaizhi(2, "在制品仓"),
    hunhe(3, "混合仓");

    private Integer code;
    private String desc;

    DeviceLocalizationEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DeviceLocalizationEnum valueof(Integer code) {
        if (code != null) {
            for (DeviceLocalizationEnum value : DeviceLocalizationEnum.values()) {
                if (value.code.equals(code)) {
                    return value;
                }
            }
        }
        return DeviceLocalizationEnum.NO;
    }
}
