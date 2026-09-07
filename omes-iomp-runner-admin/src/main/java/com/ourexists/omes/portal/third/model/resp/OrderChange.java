/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.third.model.resp;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * MES 订单变更意图（入站骨架用）。YG 无真实 API 时由 mock/空列表驱动。
 */
@Getter
@Setter
@Accessors(chain = true)
public class OrderChange {

    /** MES 变更单号，作 requestId */
    private String changeId;

    private String moCode;

    /** CANCEL_MO / RESCHEDULE / PRIORITY / QTY_UP / QTY_DOWN / CHANGE_LINE ... */
    private String changeType;

    private Boolean force;

    private Map<String, Object> payload;
}
