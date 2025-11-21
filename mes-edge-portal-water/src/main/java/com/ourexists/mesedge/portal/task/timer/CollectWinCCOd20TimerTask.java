/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task.timer;

import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.mesedge.portal.config.CacheUtils;
import com.ourexists.mesedge.portal.sync.remote.WinccApi;
import com.ourexists.mesedge.portal.sync.remote.model.Od20DevVari;
import com.ourexists.mesedge.portal.sync.remote.constants.StructureTypeEnum;
import com.ourexists.mesedge.portal.task.WinCCDevConstants;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("CollectWinCCOd20")
public class CollectWinCCOd20TimerTask extends TimerTask {

    @Autowired
    private WinccApi winccApi;

    @Autowired
    private CacheUtils cacheUtils;

    @Override
    public void doRun() {
        UserContext.defaultTenant();
        Od20DevVari datalist = winccApi.pullTags(Od20DevVari.class,  StructureTypeEnum.dev, WinCCDevConstants.OD20_CACHE);
        if (datalist == null) {
            return;
        }
        cacheUtils.put( "realtime", WinCCDevConstants.OD20_CACHE, datalist);
    }
}
