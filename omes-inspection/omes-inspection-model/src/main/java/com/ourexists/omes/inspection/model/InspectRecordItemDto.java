/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import com.ourexists.omes.inspection.enums.InspectResultEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 巡检记录明细 DTO（附表：记录id + 巡检项 + 巡检项内容）
 */
@Schema(description = "巡检记录明细")
@Getter
@Setter
@Accessors(chain = true)
public class InspectRecordItemDto extends BaseDto {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "巡检记录ID（主表）")
    private String recordId;

    @Schema(description = "巡检项ID（模板项主键）")
    private String itemId;

    @Schema(description = "巡检项名称")
    private String itemName;

    @Schema(description = "巡检项内容：填写值/结果文本")
    private String content;

    @Schema(description = "巡检结果：0正常 1异常")
    private Integer result;

    @Schema(description = "规则分值（配置项匹配得分，未乘权重）")
    private Integer ruleScore;

    @Schema(description = "该项得分（按权重/百分比权重计算后的得分）")
    private Integer score;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "照片URL，多张逗号分隔")
    private String photoUrls;

    @Schema(description = "租户ID")
    private String tenantId;

    public String getResultDesc() {
        return result != null ? InspectResultEnum.valueOf(result).getDesc() : "";
    }
}
