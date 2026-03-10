/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.portal.device.collect;

import com.ourexists.omes.device.core.workshop.cache.WorkshopRealtime;

import java.util.List;

/**
 * 场景数据解析器：在设备数据解析完成后，从同一份网关 payload 中解析场景采集配置关联的映射并更新场景实时缓存。
 * 与设备解析共用同一 payload（如 S7/WinCC/Modbus/OPC UA 返回的 JSON），按场景配置中该网关的 attr.map 取数。
 */
public interface WorkshopDataParser {

    /**
     * 解析网关数据并更新关联该网关的场景实时值。
     * 应在设备数据解析（equipDataParser.parse）之后调用。
     *
     * @param gwId       网关ID（与 connectId 一致）
     * @param sourceData 与设备解析相同的原始 payload
     */
    List<WorkshopRealtime> parse(String gwId, String sourceData);
}
