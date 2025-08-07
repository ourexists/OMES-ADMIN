/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.enums;

import com.ourexists.era.framework.webserver.enhance.I18nUtil;
import lombok.Getter;

@Getter
public enum MPSStatusEnum {

    WAIT_QUE(0, null, "${mps.status.wait.queue}"),
    WAIT_EXEC(1, 0, "${mps.status.wait.exec}"),
    EXECING(2, 1, "${mps.status.execing}"),
    COMPLETE(3, 2, "${mps.status.complete}"),
    FILE(4, 3, "${mps.status.file}"),
    ;

    private Integer code;

    private Integer preCode;

    private String name;

    MPSStatusEnum(Integer code, Integer preCode, String name) {
        this.code = code;
        this.preCode = preCode;
        this.name = name;
    }

    public static MPSStatusEnum valueOf(Integer code) {
        for (MPSStatusEnum value : MPSStatusEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return MPSStatusEnum.WAIT_QUE;
    }

    public String getName() {
        return I18nUtil.i18nParser(name);
    }
}
