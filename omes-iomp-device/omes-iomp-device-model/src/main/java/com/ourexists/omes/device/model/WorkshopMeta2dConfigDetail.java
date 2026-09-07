/*
 * Copyright (c) 2026.
 */
package com.ourexists.omes.device.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Meta2d 组态配置详情（一个场景一份）。
 * 通过 JacksonTypeHandler 以 JSON 形式存储在表字段中。
 */
@Schema
@Getter
@Setter
@Accessors(chain = true)
public class WorkshopMeta2dConfigDetail {

    /**
     * Meta2d 画布 JSON（可直接存储 meta2d 的 data/pens/page 等结构）。
     */
    private Map<String, Object> canvas;

    /**
     * 图元绑定列表。
     */
    private List<WorkshopMeta2dBinding> bindings = new ArrayList<>();

    /**
     * 运行态刷新间隔（秒）。前端可用；也可被后端聚合接口使用。
     */
    private Integer refreshIntervalSec = 2;
}

