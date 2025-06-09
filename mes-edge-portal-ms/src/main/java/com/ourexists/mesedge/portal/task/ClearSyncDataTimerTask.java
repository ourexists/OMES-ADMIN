/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task;

import com.ourexists.era.framework.core.utils.DateUtil;
import com.ourexists.mesedge.sync.manager.SyncTxStore;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component("ClearSyncData")
public class ClearSyncDataTimerTask extends TimerTask {

    @Autowired
    private SyncTxStore syncTxStore;

    @Override
    public void doRun() {
        Date date = DateUtil.getWarpDay(new Date(), -5);
        syncTxStore.clearHistory(date);
    }
}
