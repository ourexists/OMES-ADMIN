/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 巡检模板 DTO（如：水泵巡检模板）
 */
@Schema(description = "巡检模板")
@Getter
@Setter
@Accessors(chain = true)
public class InspectTemplateDto extends BaseDto {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private Date createdTime;

    @Schema(description = "更新时间")
    private Date updatedTime;

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "巡检项列表（编辑时使用）")
    private List<InspectTemplateItemDto> items;
}
