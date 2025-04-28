/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.sync.manager;

public interface SyncFlow {

    /**
     * 当前流程点位
     *
     * @return
     */
    String point();

    /**
     * 同步顺序
     *
     * @return
     */
    int sort();

    /**
     * 同步数据
     *
     * @param transfer 流转数据
     * @return
     */
    void sync(Transfer transfer);
}
