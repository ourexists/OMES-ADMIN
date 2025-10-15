/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.portal.sync.remote.WinccApi;
import com.ourexists.mesedge.portal.sync.remote.model.Datalist;
import com.ourexists.mesedge.report.feign.WinCCReportFeign;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Component("CollectWinCCDatalist")
public class CollectWinCCDatalistTimerTask extends TimerTask {

    @Autowired
    private WinccApi winccApi;

    @Autowired
    private WinCCReportFeign winCCReportFeign;

    @Override
    public void doRun() {
        UserContext.defaultTenant();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.minusSeconds(59);
        Datalist datalist = winccApi.pullDatalist(startTime, now);
        if (datalist == null) {
            return;
        }
        try {
            datalist.setStartTime(Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant()));
            datalist.setEndTime(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));
            datalist.setTime(new Date());
            RemoteHandleUtils.getDataFormResponse(winCCReportFeign.save(Datalist.covert(datalist)));
        } catch (EraCommonException e) {
            log.error(e.getMessage());
        }
    }
}
