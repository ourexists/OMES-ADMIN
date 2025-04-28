/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.enums;

import cn.hutool.core.date.DateUtil;

public enum StructTypeEnum {

    BIT,
    BOOLEAN,
    BYTE,
    WORD,
    DWORD,
    LWORD,
    REAL,
    DOUBLE,
    STRING,
    DATE;

    public static Object parse(String value, StructTypeEnum structTypeEnum) {
        switch (structTypeEnum) {
            case BIT:
                return Boolean.parseBoolean(value);
            case BOOLEAN:
                return Boolean.parseBoolean(value);
            case BYTE:
                return Byte.parseByte(value);
            case WORD:
                return Short.parseShort(value);
            case DWORD:
                return Integer.parseInt(value);
            case LWORD:
                return Long.parseLong(value);
            case REAL:
                return Float.parseFloat(value);
            case DOUBLE:
                return Double.parseDouble(value);
            case STRING:
                return value;
            case DATE:
                return DateUtil.parse(value);
            default:
                return value;
        }
    }
}
