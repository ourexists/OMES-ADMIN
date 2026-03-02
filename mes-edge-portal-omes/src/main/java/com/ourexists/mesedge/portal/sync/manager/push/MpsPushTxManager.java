/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.sync.manager.push;

import com.ourexists.era.txflow.*;
import com.ourexists.mesedge.mps.enums.MPSStatusEnum;
import com.ourexists.mesedge.mps.pojo.MPS;
import com.ourexists.mesedge.mps.pojo.MPSTF;
import com.ourexists.mesedge.mps.service.MPSService;
import com.ourexists.mesedge.mps.service.MPSTFService;
import com.ourexists.mesedge.portal.third.YGApi;
import com.ourexists.mesedge.portal.third.model.req.CompleteReq;
import com.ourexists.mesedge.sync.enums.SyncTxEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MpsPushTxManager extends TxManager {

    @Autowired
    private YGApi ygApi;

    @Autowired
    private MPSService mpsService;

    @Autowired
    private MPSTFService mpstfService;

    public MpsPushTxManager(TxStore txStore) {
        super(txStore);
    }

    @Override
    public String txName() {
        return SyncTxEnum.MPS_PUSH.name();
    }

    @Override
    protected List<TxBranchFlow> flows() {
        List<TxBranchFlow> r = new ArrayList<>();
        r.add(new AbstractTxBranchFlow(txStore) {

            @Override
            public String point() {
                return "STATE";
            }

            @Override
            public int sort() {
                return 0;
            }

            @Override
            protected void doExec(TxTransfer txTransfer) {
                String mpsId = txTransfer.getJsonData();
                mpsService.updateStatus(mpsId, MPSStatusEnum.FILE);
            }
        });
        r.add(new AbstractTxBranchFlow(txStore) {

            @Override
            public String point() {
                return "PUSH";
            }

            @Override
            public int sort() {
                return 1;
            }

            @Override
            protected void doExec(TxTransfer txTransfer) {
                String mpsId = txTransfer.getJsonData();
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
