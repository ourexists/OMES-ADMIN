/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task;

import com.ourexists.era.framework.core.utils.DateUtil;
import com.ourexists.mesedge.portal.sync.manager.pull.MoPullTxManager;
import com.ourexists.mesedge.task.process.task.TimerTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component("MoRemoteSync")
public class MoRemoteSyncTimerTask extends TimerTask {

    @Autowired
    private MoPullTxManager moPullSyncManager;

    @Override
    public void doRun() {
        moPullSyncManager.execute(DateUtil.getWarpYear(new Date(), -1), new Date());
    }
}
