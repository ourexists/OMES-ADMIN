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

/**
 * 巡检人员 DTO
 */
@Schema(description = "巡检人员")
@Getter
@Setter
@Accessors(chain = true)
public class InspectPersonDto extends BaseDto {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "关联账户ID")
    private String accountId;

    @Schema(description = "关联账户名称（展示用）")
    private String accountName;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "工号")
    private String jobNumber;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private Date createdTime;

    @Schema(description = "更新时间")
    private Date updatedTime;

    @Schema(description = "租户ID")
    private String tenantId;
}
