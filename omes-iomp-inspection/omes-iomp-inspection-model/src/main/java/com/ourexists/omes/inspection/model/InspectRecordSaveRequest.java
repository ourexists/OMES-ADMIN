/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 生成巡检记录（APP 提交）请求体
 */
@Schema(description = "生成巡检记录请求")
@Getter
@Setter
@Accessors(chain = true)
public class InspectRecordSaveRequest {

    @Schema(description = "巡检任务ID", required = true)
    @NotBlank(message = "任务ID不能为空")
    private String taskId;

    @Schema(description = "设备ID", required = true)
    @NotBlank(message = "设备ID不能为空")
    private String equipId;

    @Schema(description = "各检测项结果", required = true)
    @NotEmpty(message = "检测结果不能为空")
    @Valid
    private List<InspectRecordItemResult> results;

    @Schema(description = "单条检测项结果")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class InspectRecordItemResult {
        @Schema(description = "检测项ID（模板项主键）", required = true)
        @NotBlank(message = "检测项ID不能为空")
        private String itemId;
        @Schema(description = "填写或选择的值")
        private String value;
    }
}
