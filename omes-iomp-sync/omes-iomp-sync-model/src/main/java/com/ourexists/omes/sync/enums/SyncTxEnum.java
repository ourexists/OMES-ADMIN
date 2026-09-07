/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.sync.enums;

public enum SyncTxEnum {
    MO_PULL,
    MPS_PUSH,
    LINE_PUSH,
    PLAN_START,
    /** MES 订单变更入站 */
    MO_CHANGE,
    /** 计划异常中止出站（YG 无 abort 时为空实现） */
    PLAN_ABORT
}
