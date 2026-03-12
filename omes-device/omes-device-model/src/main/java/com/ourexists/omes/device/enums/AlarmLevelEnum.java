package com.ourexists.omes.device.enums;

import lombok.Getter;

@Getter
public enum AlarmLevelEnum {

    /** 轻微 */
    minor(0, "轻微"),
    /** 一般 */
    general(1, "一般"),
    /** 严重 */
    serious(2, "严重"),
    /** 故障 */
    fault(3, "故障");

    private final Integer code;
    private final String desc;

    AlarmLevelEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AlarmLevelEnum valueOf(Integer code) {
        if (code != null) {
            for (AlarmLevelEnum value : AlarmLevelEnum.values()) {
                if (value.code.equals(code)) {
                    return value;
                }
            }
        }
        return AlarmLevelEnum.general;
    }
}
