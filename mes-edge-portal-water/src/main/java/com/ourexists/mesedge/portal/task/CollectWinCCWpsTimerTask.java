/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.portal.sync.remote.WinccApi;
import com.ourexists.mesedge.report.feign.WinCCReportFeign;
import com.ourexists.mesedge.report.model.WinCCWpsDevDto;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component("CollectWinCCWps")
public class CollectWinCCWpsTimerTask extends TimerTask {

    @Autowired
    private WinccApi winccApi;

    @Autowired
    private WinCCReportFeign winCCReportFeign;

    @Override
    public void doRun() {
        UserContext.defaultTenant();
        WinCCWpsDevDto datalist = winccApi.pullTags("wpsDev", WinCCWpsDevDto.class, true, WinCCDevConstants.WPS_CACHE);
        if (datalist == null) {
            return;
        }
        try {
            datalist.setExecTime(new Date());
            RemoteHandleUtils.getDataFormResponse(winCCReportFeign.saveWps(datalist));
        } catch (EraCommonException e) {
            log.error(e.getMessage());
        }
    }
}
