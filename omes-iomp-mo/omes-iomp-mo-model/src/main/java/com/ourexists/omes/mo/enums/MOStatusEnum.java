/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.mo.enums;

import com.ourexists.era.framework.webserver.enhance.I18nUtil;
import lombok.Getter;

@Getter
public enum MOStatusEnum {

    INIT(0, "${mo.status.init}"),
    PART(1, "${mo.status.part}"),
    RUN(2, "${mo.status.run}"),
    COMPLETE(3, "${mo.status.complete}"),
    /**
     * 业务取消。与 COMPLETE 不同：订单未正常完成被终止。
     * 禁止用 delBit / 物理删除冒充取消。
     */
    CANCEL(4, "${mo.status.cancel}");

    private final Integer code;

    private final String name;

    MOStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static MOStatusEnum valueOf(Integer code) {
        for (MOStatusEnum value : MOStatusEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return MOStatusEnum.INIT;
    }

    public String getName() {
        return I18nUtil.i18nParser(name);
    }
}
