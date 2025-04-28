/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.sync.manager.push;

import com.ourexists.mesedge.mps.enums.MPSStatusEnum;
import com.ourexists.mesedge.mps.pojo.MPS;
import com.ourexists.mesedge.mps.pojo.MPSTF;
import com.ourexists.mesedge.mps.service.MPSService;
import com.ourexists.mesedge.mps.service.MPSTFService;
import com.ourexists.mesedge.portal.sync.manager.AbstractSyncFlow;
import com.ourexists.mesedge.portal.sync.manager.SyncFlow;
import com.ourexists.mesedge.portal.sync.manager.SyncManager;
import com.ourexists.mesedge.portal.sync.manager.Transfer;
import com.ourexists.mesedge.portal.third.YGApi;
import com.ourexists.mesedge.portal.third.model.req.CompleteReq;
import com.ourexists.mesedge.sync.enums.SyncTxEnum;
import com.ourexists.mesedge.sync.service.SyncResourceService;
import com.ourexists.mesedge.sync.service.SyncService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MpsPushSyncManager extends SyncManager {

    @Autowired
    private YGApi ygApi;

    @Autowired
    private MPSService mpsService;

    @Autowired
    private MPSTFService mpstfService;

    public MpsPushSyncManager(SyncService syncService, SyncResourceService syncResourceService) {
        super(syncService, syncResourceService);
    }

    @Override
    public String syncTx() {
        return SyncTxEnum.MPS_PUSH.name();
    }

    @Override
    protected List<SyncFlow> flows() {
        List<SyncFlow> r = new ArrayList<>();
        r.add(new AbstractSyncFlow(syncResourceService) {

            @Override
            public String point() {
                return "STATE";
            }

            @Override
            public int sort() {
                return 0;
            }

            @Override
            protected void doSync(Transfer transfer) {
                String mpsId = transfer.getJsonData();
                mpsService.updateStatus(mpsId, MPSStatusEnum.FILE);
            }
        });
        r.add(new AbstractSyncFlow(syncResourceService) {

            @Override
            public String point() {
                return "PUSH";
            }

            @Override
            public int sort() {
                return 1;
            }

            @Override
            protected void doSync(Transfer transfer) {
                String mpsId = transfer.getJsonData();
                if (StringUtils.isBlank(mpsId)) {
                    return;
                }
                MPS mps = mpsService.getById(mpsId);
                if (mps == null) {
                    return;
                }
                CompleteReq completeReq = new CompleteReq()
                        .setFrameNumber(mps.getMoCode())
                        .setProcessingRecords(MPSTF.covert(mpstfService.selectByMPSId(mpsId)));
                ygApi.pushRecord(completeReq);
            }
        });
        return r;
    }
}
