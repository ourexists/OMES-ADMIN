/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.sync.manager.push;

import com.ourexists.mesedge.portal.sync.manager.AbstractSyncFlow;
import com.ourexists.mesedge.portal.sync.manager.SyncFlow;
import com.ourexists.mesedge.portal.sync.manager.SyncManager;
import com.ourexists.mesedge.portal.sync.manager.Transfer;
import com.ourexists.mesedge.portal.third.YGApi;
import com.ourexists.mesedge.sync.enums.SyncTxEnum;
import com.ourexists.mesedge.sync.service.SyncResourceService;
import com.ourexists.mesedge.sync.service.SyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PlanNotifySyncManager extends SyncManager {

    @Autowired
    private YGApi ygApi;

    public PlanNotifySyncManager(SyncService syncService, SyncResourceService syncResourceService) {
        super(syncService, syncResourceService);
    }

    @Override
    public String syncTx() {
        return SyncTxEnum.PLAN_START.name();
    }

    @Override
    protected List<SyncFlow> flows() {
        List<SyncFlow> r = new ArrayList<>();
        r.add(new AbstractSyncFlow(syncResourceService) {

            @Override
            public String point() {
                return "PUSH";
            }

            @Override
            public int sort() {
                return 0;
            }

            @Override
            protected void doSync(Transfer transfer) {
                String moCode = transfer.getJsonData();

                ygApi.startPlan(moCode);
            }
        });
        return r;
    }
}
