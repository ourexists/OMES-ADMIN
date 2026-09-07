/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.mo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Schema(description = "MO surplus 对账项")
@Getter
@Setter
@Accessors(chain = true)
public class MoSurplusReconcileItem {

    private String moCode;

    private Integer num;

    private Integer surplus;

    private Integer activeMpsSum;

    private Integer expectedSurplus;

    private String status;

    private String message;
}
