/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task;

import com.ourexists.era.framework.core.utils.DateUtil;
import com.ourexists.mesedge.sync.service.SyncService;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component("ClearSyncData")
public class ClearSyncDataTimerTask extends TimerTask {

    @Autowired
    private SyncService syncService;

    @Override
    public void doRun() {
        Date date = DateUtil.getWarpDay(new Date(), -5);
        syncService.clearHistory(date);
    }
}
