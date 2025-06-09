/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task;


import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.line.enums.TFTypeEnum;
import com.ourexists.mesedge.mps.enums.MPSStatusEnum;
import com.ourexists.mesedge.mps.enums.MPSTFStatusEnum;
import com.ourexists.mesedge.mps.feign.MPSFeign;
import com.ourexists.mesedge.mps.feign.MPSTFFeign;
import com.ourexists.mesedge.mps.model.MPSDto;
import com.ourexists.mesedge.mps.model.MPSTFVo;
import com.ourexists.mesedge.portal.sync.push.PlanNotifyTxManager;
import com.ourexists.mesedge.sync.enums.SyncTxEnum;
import com.ourexists.mesedge.sync.service.SyncResourceService;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component("MpsTfStatus")
public class MpsTfStatusTimerTask extends TimerTask {

    @Autowired
    private MPSTFFeign mpstfFeign;

    @Autowired
    private MPSFeign mpsFeign;

    @Autowired
    private PlanNotifyTxManager planNotifySyncManager;

    @Autowired
    private SyncResourceService syncFeign;

    @Override
    public void doRun() {
        try {
            List<MPSDto> mpsList = RemoteHandleUtils.getDataFormResponse(mpsFeign.selectByStatus(MPSStatusEnum.EXECING));
            for (MPSDto mps : mpsList) {
                List<MPSTFVo> mpstfs = mps.getTfs();
                if (CollectionUtil.isNotBlank(mpstfs)) {
                    for (int i = 0; i < mpstfs.size(); i++) {
                        MPSTFVo mpstf = mpstfs.get(i);
                        if (i == 0 && mpstf.getStatus().equals(MPSTFStatusEnum.EXEC.getCode())) {
                            Boolean exist = syncFeign.existSync(SyncTxEnum.PLAN_START, mpstf.getMoCode());
                            if (!exist) {
                                planNotifySyncManager.execute(mpstf.getMoCode());
                            }
                        }
                        if (mpstf.getStatus().equals(MPSTFStatusEnum.COMMON.getCode())
                                || mpstf.getStatus().equals(MPSTFStatusEnum.EXEC.getCode())) {
                            //按顺序一直向下走，直到当前流程为其它状态
                            if (mpstf.getType().equals(TFTypeEnum.NO_ACTION.getCode())) {
                                try {
                                    RemoteHandleUtils.getDataFormResponse(mpstfFeign.updateStatus(mpstf.getId(), MPSTFStatusEnum.COMPLETE));
                                } catch (EraCommonException e) {
                                    log.error(e.getMessage(), e);
                                    return;
                                }
                            } else {
                                break;
                            }
                        } else if (!mpstf.getStatus().equals(MPSTFStatusEnum.COMPLETE.getCode())) {
                            //当前流程处于其它状态,直接终止
                            break;
                        }
                    }
                }
            }
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
        }
    }
}
