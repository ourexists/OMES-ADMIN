package com.ourexists.mesedge.device.enums;

import com.ourexists.era.framework.webserver.enhance.I18nUtil;
import lombok.Getter;

@Getter
public enum DeviceLocalizationEnum {

    NO(0, "${device.localization.no}"),
    yuanliao(1, "${device.localization.yuanliao}"),
    zaizhi(2, "${device.localization.zaizhi}"),
    hunhe(3, "${device.localization.hunhe}");

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

    public String getDesc() {
        return I18nUtil.i18nParser(desc);
    }
}
