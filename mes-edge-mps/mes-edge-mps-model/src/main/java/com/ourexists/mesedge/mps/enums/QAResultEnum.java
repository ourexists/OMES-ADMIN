/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.enums;

import com.ourexists.era.framework.webserver.enhance.I18nUtil;
import lombok.Getter;

@Getter
public enum QAResultEnum {

    NOT(0, "${qa.result.not}"),
    PASS(1, "${qa.result.pass}"),
    NOPASS(2, "${qa.result.nopass}");

    private final Integer code;

    private final String name;

    QAResultEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static QAResultEnum valueOf(Integer code) {
        for (QAResultEnum value : QAResultEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return QAResultEnum.NOT;
    }

    public String getName() {
        return I18nUtil.i18nParser(name);
    }
}
