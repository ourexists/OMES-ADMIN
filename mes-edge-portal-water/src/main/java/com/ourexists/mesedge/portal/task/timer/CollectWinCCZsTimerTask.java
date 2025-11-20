/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task.timer;

import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.mesedge.portal.config.CacheUtils;
import com.ourexists.mesedge.portal.sync.remote.WinccApi;
import com.ourexists.mesedge.portal.task.WinCCDevConstants;
import com.ourexists.mesedge.report.model.WinCCZsDevDto;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("CollectWinCCZs")
public class CollectWinCCZsTimerTask extends TimerTask {

    @Autowired
    private WinccApi winccApi;

    @Autowired
    private CacheUtils cacheUtils;

    @Override
    public void doRun() {
        UserContext.defaultTenant();
        WinCCZsDevDto datalist = winccApi.pullArchive("zsDev", WinCCZsDevDto.class, true, WinCCDevConstants.ZS_CACHE);
        if (datalist == null) {
            return;
        }
        cacheUtils.put("realtime", WinCCDevConstants.ZS_CACHE, datalist);
    }
}
