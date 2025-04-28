/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.mps.feign.MPSFeign;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("MpsJoinQue")
public class MpsJoinQueTimerTask extends TimerTask {

    @Autowired
    private MPSFeign mpsFeign;

    @Override
    public void doRun() {
        try {
            RemoteHandleUtils.getDataFormResponse(mpsFeign.adjustToJoinQue());
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
        }
    }
}
