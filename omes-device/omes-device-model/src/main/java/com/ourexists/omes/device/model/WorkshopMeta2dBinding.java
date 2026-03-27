/*
 * Copyright (c) 2026.
 */
package com.ourexists.omes.device.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * Meta2d 图元与数据源绑定定义（存入 JSON 配置中）。
 */
@Schema
@Getter
@Setter
@Accessors(chain = true)
public class WorkshopMeta2dBinding {

    /**
     * Meta2d pen.id
     */
    private String penId;

    /**
     * 绑定到 pen 的哪个属性（如 text / color / visible / image / progress 等）。
     */
    private String prop;

    /**
     * 数据源类型：
     * - WORKSHOP_ATTR: 场景实时采集项（来自 WorkshopRealtimeCollect）
     * - EQUIP_REALTIME: 设备实时态（来自 EquipRealtime）
     * - CONST/EXPR: 常量/表达式（预留）
     */
    private String sourceType;

    /**
     * 数据源 key（建议用 attr.map 作为稳定 key；或设备字段名/点位 key）。
     */
    private String sourceKey;

    /**
     * 额外参数（如 format、mapping、expr、threshold、fallback 等）。
     */
    private Map<String, Object> options;
}

