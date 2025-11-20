/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task.timer;

import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.mesedge.portal.config.CacheUtils;
import com.ourexists.mesedge.portal.sync.remote.WinccApi;
import com.ourexists.mesedge.portal.sync.remote.model.DosingDevVari;
import com.ourexists.mesedge.portal.task.WinCCDevConstants;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("CollectWinCCDosing")
public class CollectWinCCDosingTimerTask extends TimerTask {

    @Autowired
    private WinccApi winccApi;

    @Autowired
    private CacheUtils cacheUtils;

    @Override
    public void doRun() {
        UserContext.defaultTenant();
        DosingDevVari datalist = winccApi.pullTags(DosingDevVari.class, true, WinCCDevConstants.DOSING_CACHE);
        if (datalist == null) {
            return;
        }
        cacheUtils.put("realtime", WinCCDevConstants.DOSING_CACHE, datalist);
    }
}
