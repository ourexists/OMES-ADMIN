/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.enums;

import lombok.Getter;

/**
 * 边缘设备连接/同步支持的协议类型，用于连接管理(Connect)及数据采集通道。
 */
@Getter
public enum ProtocolEnum {

    /** OPC UA */
    opc_ua("OPC UA"),
    /** REST/HTTP */
    rest("REST"),
    /** 西门子 WINCC */
    WINCC("WINCC"),
    /** MQTT */
    MQTT("MQTT"),
    /** Modbus TCP */
    modbus_tcp("Modbus TCP"),
    /** Modbus RTU */
    modbus_rtu("Modbus RTU"),
    /** OPC DA（经典 OPC） */
    opc_da("OPC DA"),
    /** 西门子 S7 */
    s7("S7");

    private final String desc;

    ProtocolEnum(String desc) {
        this.desc = desc;
    }
}
