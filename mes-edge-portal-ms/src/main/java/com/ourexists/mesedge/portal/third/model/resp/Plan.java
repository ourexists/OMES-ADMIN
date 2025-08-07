/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.third.model.resp;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class Plan {

    /**
     * 物料唯一码
     */
    private String barCode;

    /**
     * 物料任务唯一码
     */
    private String recordNumber;

    /**
     * 计划开始时间
     */
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date planStartTime;

    /**
     * 计划结束时间
     */
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date planEndTime;

    /**
     * 工步计划时间
     */
    private List<StepPlanTime> stepPlanTime;
}
