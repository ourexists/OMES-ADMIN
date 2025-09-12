package com.ourexists.mesedge.device.enums;

import com.ourexists.era.framework.webserver.enhance.I18nUtil;
import lombok.Getter;

@Getter
public enum DeviceStatusEnum {

    enable(0, "${common.status.enabled}"),
    disable(1, "${common.status.disabled}");
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

    public String getDesc() {
        return I18nUtil.i18nParser(desc);
    }
}
