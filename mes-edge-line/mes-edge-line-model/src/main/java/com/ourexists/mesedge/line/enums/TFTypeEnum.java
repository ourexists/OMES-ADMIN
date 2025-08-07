/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.line.enums;

import com.ourexists.era.framework.webserver.enhance.I18nUtil;
import lombok.Getter;

@Getter
public enum TFTypeEnum {

    SYSTEM(0, "${tf.type.system}"),
    QA(2, "${tf.type.qa}"),
    NO_ACTION(1, "${tf.type.no_action}"),
    ;

    private final Integer code;

    private final String name;

    TFTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static TFTypeEnum valueOf(Integer code) {
        for (TFTypeEnum value : TFTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return TFTypeEnum.SYSTEM;
    }

    public String getName() {
        return I18nUtil.i18nParser(name);
    }
}
