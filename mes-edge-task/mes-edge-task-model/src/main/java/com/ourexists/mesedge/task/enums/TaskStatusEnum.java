/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.task.enums;

import com.ourexists.era.framework.webserver.enhance.I18nUtil;
import lombok.Getter;

@Getter
public enum TaskStatusEnum {

    STOPPING(0, "${task.status.stopping}"),
    RUNNING(1, "${task.status.running}");

    private final Integer code;

    private final String name;

    TaskStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static TaskStatusEnum valueOf(Integer code) {
        for (TaskStatusEnum value : TaskStatusEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return TaskStatusEnum.STOPPING;
    }

    public String getName() {
        return I18nUtil.i18nParser(name);
    }
}
