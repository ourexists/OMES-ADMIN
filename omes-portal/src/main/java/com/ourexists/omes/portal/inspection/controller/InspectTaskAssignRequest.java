/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.inspection.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Schema(description = "任务指派请求")
@Getter
@Setter
public class InspectTaskAssignRequest {

    @Schema(description = "任务ID列表")
    private List<String> taskIds;

    @Schema(description = "指派的巡检人员ID")
    private String personId;
}
