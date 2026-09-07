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

@Schema(description = "调整预览：只算影响面不落库")
@Getter
@Setter
@Accessors(chain = true)
public class MoAdjustPreviewResult {

    private String moCode;

    private String adjustType;

    private boolean allowed = true;

    private String rejectReason;

    private List<String> wouldVoidMpsIds = new ArrayList<>();

    private List<String> wouldAffectMpsIds = new ArrayList<>();

    private Integer surplusDelta;

    private Integer newNum;

    private Integer newSurplus;

    private List<String> hints = new ArrayList<>();

    private List<String> warnings = new ArrayList<>();

    private boolean requiresForce;
}
