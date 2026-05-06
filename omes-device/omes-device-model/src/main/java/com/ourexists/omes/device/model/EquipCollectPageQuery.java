/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.model;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;


@Schema
@Getter
@Setter
@Accessors(chain = true)
public class EquipCollectPageQuery extends PageQuery {

    @Schema(description = "设备编号")
    private String sn;

    private Date startDate;

    private Date endDate;

    /**
     * 数据表 / 查询聚合粒度：空或 RAW 为原始采集点；MIN30、HOUR、DAY、MONTH 为统计聚合（均值等）；
     * AUTO 为趋势图专用：由起止时间跨度在后台自动选桶，且每桶取<strong>最后一个</strong>采样点。
     */
    @Schema(description = "采集数据聚合：RAW|MIN30|HOUR|DAY|MONTH|AUTO（AUTO=按跨度自动分桶+每桶末点）")
    private String aggregateInterval;

}
