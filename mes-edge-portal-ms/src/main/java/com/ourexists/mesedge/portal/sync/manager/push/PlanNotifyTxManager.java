/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.sync.manager.push;

import com.ourexists.era.txflow.*;
import com.ourexists.mesedge.portal.sync.manager.*;
import com.ourexists.mesedge.portal.third.YGApi;
import com.ourexists.mesedge.sync.enums.SyncTxEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PlanNotifyTxManager extends TxManager {

    @Autowired
    private YGApi ygApi;

    public PlanNotifyTxManager(TxStore txStore) {
        super(txStore);
    }

    @Override
    public String txName() {
        return SyncTxEnum.PLAN_START.name();
    }

    @Override
    protected List<TxBranchFlow> flows() {
        List<TxBranchFlow> r = new ArrayList<>();
        r.add(new AbstractTxBranchFlow(txStore) {

            @Override
            public String point() {
                return "PUSH";
            }

            @Override
            public int sort() {
                return 0;
            }

            @Override
            protected void doExec(TxTransfer txTransfer) {
                String moCode = txTransfer.getJsonData();

                ygApi.startPlan(moCode);
            }
        });
        return r;
    }
}
