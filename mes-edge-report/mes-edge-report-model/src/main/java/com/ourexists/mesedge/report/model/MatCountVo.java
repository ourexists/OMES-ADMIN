/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 配方表
 */
@Getter
@Setter
@Accessors(chain = true)
public class MatCountVo {


    /**
     * 原料名
     */
    private String lm;

    /**
     * 目标值
     */
    private Float ll;

    /**
     * 实际值
     */
    private Float sj;
}
