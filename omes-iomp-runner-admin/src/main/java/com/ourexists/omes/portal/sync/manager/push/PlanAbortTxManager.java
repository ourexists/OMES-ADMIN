/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.sync.manager.push;

import com.ourexists.era.txflow.AbstractTxBranchFlow;
import com.ourexists.era.txflow.TxBranchFlow;
import com.ourexists.era.txflow.TxManager;
import com.ourexists.era.txflow.TxStore;
import com.ourexists.era.txflow.TxTransfer;
import com.ourexists.omes.portal.third.YGApi;
import com.ourexists.omes.sync.enums.SyncTxEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 计划异常中止出站骨架。YG 当前无 abort API：本地记日志 + 调用 stub。
 */
@Slf4j
@Component
public class PlanAbortTxManager extends TxManager {

    @Autowired
    private YGApi ygApi;

    public PlanAbortTxManager(TxStore txStore) {
        super(txStore);
    }

    @Override
    public String txName() {
        return SyncTxEnum.PLAN_ABORT.name();
    }

    @Override
    protected List<TxBranchFlow> flows() {
        List<TxBranchFlow> r = new ArrayList<>();
        r.add(new AbstractTxBranchFlow(txStore) {
            @Override
            public String point() {
                return "ABORT";
            }

            @Override
            public int sort() {
                return 0;
            }

            @Override
            protected void doExec(TxTransfer txTransfer) {
                String moCode = txTransfer.getJsonData();
                if (StringUtils.isBlank(moCode)) {
                    return;
                }
                // TODO: 对接 YG abort API 后在此推送；当前仅 stub
                ygApi.abortPlan(moCode);
                log.warn("PLAN_ABORT stub executed for moCode={}", moCode);
            }
        });
        return r;
    }
}
