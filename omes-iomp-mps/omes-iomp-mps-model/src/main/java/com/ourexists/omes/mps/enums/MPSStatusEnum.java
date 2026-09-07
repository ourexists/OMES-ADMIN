/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.mps.enums;

import com.ourexists.era.framework.webserver.enhance.I18nUtil;
import lombok.Getter;

@Getter
public enum MPSStatusEnum {

    WAIT_QUE(0, null, "${mps.status.wait.queue}"),
    WAIT_EXEC(1, 0, "${mps.status.wait.exec}"),
    EXECING(2, 1, "${mps.status.execing}"),
    COMPLETE(3, 2, "${mps.status.complete}"),
    /**
     * 正常完成并已推送/归档。与 CANCEL 语义不同，禁止混用。
     */
    FILE(4, 3, "${mps.status.file}"),
    /**
     * 业务作废/取消。未开工批次作废后进入此状态；不走 COMPLETE→FILE 推送路径。
     * CANCEL ≠ FILE。preCode 为 null：由 voidMpsCascade 对 WAIT_QUE/WAIT_EXEC 做 CAS，不走线性状态机。
     */
    CANCEL(5, null, "${mps.status.cancel}"),
    ;

    private final Integer code;

    private final Integer preCode;

    private final String name;

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
