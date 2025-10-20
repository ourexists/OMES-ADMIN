/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.portal.sync.remote.WinccApi;
import com.ourexists.mesedge.report.feign.WinCCReportFeign;
import com.ourexists.mesedge.report.model.WinCCOd11DevDto;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@Slf4j
@Component("CollectWinCCOd11")
public class CollectWinCCOd11TimerTask extends TimerTask {

    @Autowired
    private WinccApi winccApi;

    @Autowired
    private WinCCReportFeign winCCReportFeign;

    @Override
    public void doRun() {
        UserContext.defaultTenant();
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime nowGMT = LocalDateTime.now().atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("GMT"));
        LocalDateTime startTime = now.minusSeconds(59);
        ZonedDateTime startGMT = startTime.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("GMT"));
        WinCCOd11DevDto datalist = winccApi.pullTags("od11Dev", WinCCOd11DevDto.class, startGMT, nowGMT);
        if (datalist == null) {
            return;
        }
        try {
            datalist.setStartTime(Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant()));
            datalist.setEndTime(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));
            datalist.setExecTime(new Date());
            RemoteHandleUtils.getDataFormResponse(winCCReportFeign.saveOd11(datalist));
        } catch (EraCommonException e) {
            log.error(e.getMessage());
        }
    }
}
