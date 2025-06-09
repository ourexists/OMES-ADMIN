/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.mps.enums.MPSStatusEnum;
import com.ourexists.mesedge.mps.feign.MPSFeign;
import com.ourexists.mesedge.mps.model.MPSDto;
import com.ourexists.mesedge.portal.sync.push.MpsPushTxManager;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component("MpsComplete")
public class MpsCompleteTimerTask extends TimerTask {

    @Autowired
    private MPSFeign mpsFeign;

    @Autowired
    private MpsPushTxManager mpsPushSyncManager;

    @Override
    public void doRun() {
        List<MPSDto> mpsList = null;
        try {
            mpsList = RemoteHandleUtils.getDataFormResponse(mpsFeign.selectByStatus(MPSStatusEnum.COMPLETE));
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
            return;
        }
        if (CollectionUtil.isBlank(mpsList)) {
            return;
        }
        //远程同步
        for (MPSDto mps : mpsList) {
            mpsPushSyncManager.execute(mps.getId());
        }
    }
}
