/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task.timer;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.portal.config.CacheUtils;
import com.ourexists.mesedge.portal.sync.remote.model.DosingDevVari;
import com.ourexists.mesedge.portal.task.WinCCDevConstants;
import com.ourexists.mesedge.report.feign.WinCCReportFeign;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("StoreWinCCDosing")
public class StoreWinCCDosingTimerTask extends TimerTask {

    @Autowired
    private WinCCReportFeign winCCReportFeign;

    @Autowired
    private CacheUtils cacheUtils;

    @Override
    public void doRun() {
        UserContext.defaultTenant();
        DosingDevVari datalist = cacheUtils.get("realtime", WinCCDevConstants.DOSING_CACHE);
        if (datalist == null) {
            return;
        }
        try {
            RemoteHandleUtils.getDataFormResponse(winCCReportFeign.saveDosing(DosingDevVari.covert(datalist)));
        } catch (EraCommonException e) {
            log.error(e.getMessage());
        }
    }
}
