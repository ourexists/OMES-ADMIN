/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mo.enums;

import com.ourexists.era.framework.webmvc.I18nUtil;
import lombok.Getter;

@Getter
public enum MoSplitEnum {

    disposable(0, "${mo.split.disposable}"),
    part(1, "${mo.split.part}");

    private final Integer code;

    private final String name;

    MoSplitEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static MoSplitEnum valueOf(Integer code) {
        for (MoSplitEnum value : MoSplitEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return MoSplitEnum.disposable;
    }

    public String getName() {
        return I18nUtil.i18nParser(name);
    }
}
