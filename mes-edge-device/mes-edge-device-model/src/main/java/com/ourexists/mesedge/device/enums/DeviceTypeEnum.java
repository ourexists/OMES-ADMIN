package com.ourexists.mesedge.device.enums;

import com.ourexists.era.framework.webserver.enhance.I18nUtil;
import lombok.Getter;

@Getter
public enum DeviceTypeEnum {

    cang(0, "${device.type.cang}"),
    cheng(1, "${device.type.cheng}");
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

    public String getDesc() {
        return I18nUtil.i18nParser(desc);
    }
}
