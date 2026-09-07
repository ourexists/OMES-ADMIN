/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.mo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "生产订单调整结果")
@Getter
@Setter
@Accessors(chain = true)
public class MoAdjustResult {

    private String logId;

    private String requestId;

    private String moCode;

    private String adjustType;

    private MODto mo;

    private List<String> voidedMpsIds = new ArrayList<>();

    private List<String> affectedMpsIds = new ArrayList<>();

    private List<String> createdMpsIds = new ArrayList<>();

    private List<String> hints = new ArrayList<>();

    private List<String> warnings = new ArrayList<>();

    private Integer surplusDelta;

    private boolean idempotentReplay;

    private boolean preview;
}
