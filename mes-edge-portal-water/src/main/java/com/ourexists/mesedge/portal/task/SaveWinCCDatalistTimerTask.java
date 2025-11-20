/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.portal.config.CacheUtils;
import com.ourexists.mesedge.report.feign.WinCCReportFeign;
import com.ourexists.mesedge.report.model.WinCCDatalistDto;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.ourexists.mesedge.portal.task.WinCCDevConstants.DATA_CACHE;

@Slf4j
@Component("SaveWinCCDatalist")
public class SaveWinCCDatalistTimerTask extends TimerTask {

    @Autowired
    private WinCCReportFeign winCCReportFeign;

    @Autowired
    private CacheUtils cacheUtils;

    @Override
    public void doRun() {
        UserContext.defaultTenant();
        WinCCDatalistDto r =cacheUtils.get(DATA_CACHE, "dataList");
        if (r == null) {
            return;
        }
        r.setExecTime(new Date());
        try {
            RemoteHandleUtils.getDataFormResponse(winCCReportFeign.saveDataList(r));
        } catch (EraCommonException e) {
            log.error(e.getMessage());
        }
    }
}
