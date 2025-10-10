/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.utils.DateUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.portal.sync.remote.WinccApi;
import com.ourexists.mesedge.portal.sync.remote.model.Datalist;
import com.ourexists.mesedge.report.feign.WinCCReportFeign;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        Date now = new Date();
        Date startTime = DateUtil.getWarpSecond(now, -59);
        Datalist datalist = winccApi.pullDatalist(startTime, now);
        try {
            RemoteHandleUtils.getDataFormResponse(winCCReportFeign.save(Datalist.covert(datalist)));
        } catch (EraCommonException e) {
            log.error(e.getMessage());
        }
    }
}
