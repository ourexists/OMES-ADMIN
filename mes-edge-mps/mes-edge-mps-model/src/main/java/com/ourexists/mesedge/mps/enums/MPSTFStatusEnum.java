/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.enums;

import com.ourexists.era.framework.webmvc.I18nUtil;
import lombok.Getter;

@Getter
public enum MPSTFStatusEnum {

    COMMON(0, null, "${mpstf.status.common}"),
    EXEC(1, 0, "${mpstf.status.exec}"),
    COMPLETE(2, 1, "${mpstf.status.complete}"),
    STOP(3, 2, "${mpstf.status.stop}"),
    BREAK(4, 3, "${mpstf.status.break}");

    private Integer code;

    private Integer preCode;

    private String name;

    MPSTFStatusEnum(Integer code, Integer preCode, String name) {
        this.code = code;
        this.preCode = preCode;
        this.name = name;
    }

    public static MPSTFStatusEnum valueOf(Integer code) {
        for (MPSTFStatusEnum value : MPSTFStatusEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return MPSTFStatusEnum.COMMON;
    }

    public String getName() {
        return I18nUtil.i18nParser(name);
    }
}
