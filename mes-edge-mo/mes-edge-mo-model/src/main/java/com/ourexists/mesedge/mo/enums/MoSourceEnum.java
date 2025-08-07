/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mo.enums;

import com.ourexists.era.framework.webserver.enhance.I18nUtil;
import lombok.Getter;

@Getter
public enum MoSourceEnum {

    SYSTEM(0, "${mo.source.system}"),
    MSE(1, "MSE");

    private Integer code;

    private String name;

    MoSourceEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static MoSourceEnum valueOf(Integer code) {
        for (MoSourceEnum value : MoSourceEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return MoSourceEnum.SYSTEM;
    }

    public String getName() {
        return I18nUtil.i18nParser(name);
    }
}
