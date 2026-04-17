package com.ourexists.omes.ai.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiInspectionReportRequest {

    private Integer days;

    private Integer limit;

    private Boolean includeOnlyAbnormal;

    /**
     * 对接原巡检场景：按任务过滤
     */
    private String taskId;

    /**
     * 对接原巡检场景：按设备名称/编号模糊过滤
     */
    private String equipName;
}
