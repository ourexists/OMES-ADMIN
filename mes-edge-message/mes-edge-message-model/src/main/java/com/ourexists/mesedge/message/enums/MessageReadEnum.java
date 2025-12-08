package com.ourexists.mesedge.message.enums;

import lombok.Getter;

public enum MessageReadEnum {

    unread(0),
    read(1);

    @Getter
    private final Integer code;

    MessageReadEnum(int code) {
        this.code = code;
    }

    public static MessageReadEnum valueOf(Integer code) {
        for (MessageReadEnum value : MessageReadEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return MessageReadEnum.unread;
    }
}
