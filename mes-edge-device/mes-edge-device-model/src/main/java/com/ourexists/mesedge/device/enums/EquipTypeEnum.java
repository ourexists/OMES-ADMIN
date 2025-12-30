package com.ourexists.mesedge.device.enums;

public enum EquipTypeEnum {

    def(0, "通用设备"),
    yw(1, "液位计");

    private final Integer code;

    private final String desc;

    EquipTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static EquipTypeEnum valueof(Integer code) {
        if (code != null) {
            for (EquipTypeEnum value : EquipTypeEnum.values()) {
                if (value.code.equals(code)) {
                    return value;
                }
            }
        }
        return EquipTypeEnum.def;
    }
}
