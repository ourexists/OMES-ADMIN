/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.third.model.resp;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class StepPlanTime {

    private String stepName;

    private String stepOrder;

    private Date planStartTime;

    private Date planEndTime;
}
