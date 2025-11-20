/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task.timer;

import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.mesedge.portal.config.CacheUtils;
import com.ourexists.mesedge.portal.sync.remote.WinccApi;
import com.ourexists.mesedge.portal.sync.remote.model.Od12DevVari;
import com.ourexists.mesedge.portal.task.WinCCDevConstants;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("CollectWinCCOd12")
public class CollectWinCCOd12TimerTask extends TimerTask {

    @Autowired
    private WinccApi winccApi;

    @Autowired
    private CacheUtils cacheUtils;

    @Override
    public void doRun() {
        UserContext.defaultTenant();
        Od12DevVari datalist = winccApi.pullTags(Od12DevVari.class, true, WinCCDevConstants.OD12_CACHE);
        if (datalist == null) {
            return;
        }
        cacheUtils.put("realtime", WinCCDevConstants.OD12_CACHE, datalist);
    }
}
