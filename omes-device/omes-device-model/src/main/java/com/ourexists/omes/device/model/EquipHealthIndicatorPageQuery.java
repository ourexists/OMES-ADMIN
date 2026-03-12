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

/**
 * 设备健康指标：某统计周期内的健康得分与明细
 */
@Schema(description = "设备健康指标分页")
@Getter
@Setter
@Accessors(chain = true)
public class EquipHealthIndicatorPageQuery extends PageQuery {

    @Schema(description = "统计时间")
    private Date statTime;
}
