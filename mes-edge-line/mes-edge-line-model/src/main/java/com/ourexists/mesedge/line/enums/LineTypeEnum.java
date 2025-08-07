/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.line.enums;

import com.ourexists.era.framework.webserver.enhance.I18nUtil;
import lombok.Getter;

@Getter
public enum LineTypeEnum {

    FIX(0, "${line.type.fix}"),
    PARAM(1, "${line.type.param}"),
    CHANGE(2, "${line.type.change}");

    private Integer code;

    private String name;

    LineTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static LineTypeEnum valueOf(Integer code) {
        for (LineTypeEnum value : LineTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return LineTypeEnum.FIX;
    }

    public String getName() {
        return I18nUtil.i18nParser(name);
    }
}
