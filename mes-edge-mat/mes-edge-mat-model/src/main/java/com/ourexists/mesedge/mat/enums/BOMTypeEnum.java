/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.enums;

import com.ourexists.era.framework.webmvc.I18nUtil;
import lombok.Getter;

@Getter
public enum BOMTypeEnum {

    NUM(0, "${bom.type.num}"),
    PER(1, "${bom.type.perfec}");
//    TMP(2, "${bom.type.tmp}");

    private final Integer code;
    private final String name;

    BOMTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static BOMTypeEnum valueOf(Integer code) {
        for (BOMTypeEnum value : BOMTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return BOMTypeEnum.NUM;
    }

    public String getName() {
        return I18nUtil.i18nParser(name);
    }
}
