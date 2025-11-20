/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task.timer;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.portal.config.CacheUtils;
import com.ourexists.mesedge.portal.sync.remote.model.WpsDevVari;
import com.ourexists.mesedge.portal.task.WinCCDevConstants;
import com.ourexists.mesedge.report.feign.WinCCReportFeign;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("StoreWinCCWps")
public class StoreWinCCWpsTimerTask extends TimerTask {

    @Autowired
    private CacheUtils cacheUtils;

    @Autowired
    private WinCCReportFeign winCCReportFeign;

    @Override
    public void doRun() {
        UserContext.defaultTenant();
        WpsDevVari datalist = cacheUtils.get("realtime", WinCCDevConstants.WPS_CACHE);
        if (datalist == null) {
            return;
        }
        try {
            RemoteHandleUtils.getDataFormResponse(winCCReportFeign.saveWps(WpsDevVari.covert(datalist)));
        } catch (EraCommonException e) {
            log.error(e.getMessage());
        }
    }
}
