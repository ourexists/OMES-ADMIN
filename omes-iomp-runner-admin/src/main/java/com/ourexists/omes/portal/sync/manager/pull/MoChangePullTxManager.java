/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.sync.manager.pull;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.txflow.AbstractTxBranchFlow;
import com.ourexists.era.txflow.TxBranchFlow;
import com.ourexists.era.txflow.TxManager;
import com.ourexists.era.txflow.TxStore;
import com.ourexists.era.txflow.TxTransfer;
import com.ourexists.omes.mo.enums.MoAdjustSourceEnum;
import com.ourexists.omes.mo.enums.MoAdjustTypeEnum;
import com.ourexists.omes.mo.model.MoAdjustCommand;
import com.ourexists.omes.portal.flow.MoAdjustOrchestrator;
import com.ourexists.omes.portal.third.YGApi;
import com.ourexists.omes.portal.third.model.resp.OrderChange;
import com.ourexists.omes.sync.enums.SyncTxEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MES 订单变更入站骨架：DetectDiff → BuildCommand → Apply → Ack。
 * YG 无真实变更 API 时 {@link YGApi#selectOrderChanges} 返回空列表，不会自动拉取消破坏生产。
 */
@Slf4j
@Component
public class MoChangePullTxManager extends TxManager {

    @Autowired
    private YGApi ygApi;

    @Autowired
    private MoAdjustOrchestrator moAdjustOrchestrator;

    public MoChangePullTxManager(TxStore txStore) {
        super(txStore);
    }

    @Override
    public String txName() {
        return SyncTxEnum.MO_CHANGE.name();
    }

    @Override
    protected List<TxBranchFlow> flows() {
        List<TxBranchFlow> r = new ArrayList<>();
        r.add(new AbstractTxBranchFlow(txStore) {
            @Override
            public String point() {
                return "DetectDiff";
            }

            @Override
            public int sort() {
                return 0;
            }

            @Override
            protected void doExec(TxTransfer txTransfer) {
                List<OrderChange> changes = ygApi.selectOrderChanges(
                        txTransfer.getTx().getPartStartTimestamp(),
                        txTransfer.getTx().getPartEndTimestamp());
                if (CollectionUtil.isBlank(changes)) {
                    log.info("MO_CHANGE DetectDiff: no changes (mock/empty)");
                    txTransfer.setJsonData("[]");
                    return;
                }
                txTransfer.setJsonData(JSON.toJSONString(changes));
            }
        });
        r.add(new AbstractTxBranchFlow(txStore) {
            @Override
            public String point() {
                return "BuildCommand";
            }

            @Override
            public int sort() {
                return 1;
            }

            @Override
            protected void doExec(TxTransfer txTransfer) {
                if (StringUtils.isBlank(txTransfer.getJsonData())) {
                    return;
                }
                List<OrderChange> changes = JSON.parseArray(txTransfer.getJsonData(), OrderChange.class);
                List<MoAdjustCommand> commands = new ArrayList<>();
                if (CollectionUtil.isNotBlank(changes)) {
                    for (OrderChange change : changes) {
                        MoAdjustCommand cmd = toCommand(change);
                        if (cmd != null) {
                            commands.add(cmd);
                        }
                    }
                }
                txTransfer.setJsonData(JSON.toJSONString(commands));
            }
        });
        r.add(new AbstractTxBranchFlow(txStore) {
            @Override
            public String point() {
                return "Apply";
            }

            @Override
            public int sort() {
                return 2;
            }

            @Override
            protected void doExec(TxTransfer txTransfer) {
                if (StringUtils.isBlank(txTransfer.getJsonData())) {
                    return;
                }
                List<MoAdjustCommand> commands = JSON.parseArray(txTransfer.getJsonData(), MoAdjustCommand.class);
                if (CollectionUtil.isBlank(commands)) {
                    return;
                }
                for (MoAdjustCommand cmd : commands) {
                    moAdjustOrchestrator.adjust(cmd);
                }
            }
        });
        r.add(new AbstractTxBranchFlow(txStore) {
            @Override
            public String point() {
                return "Ack";
            }

            @Override
            public int sort() {
                return 3;
            }

            @Override
            protected void doExec(TxTransfer txTransfer) {
                // TODO: 对接 YG ack 变更消费确认。当前仅日志。
                log.info("MO_CHANGE Ack: processed payload length={}",
                        txTransfer.getJsonData() == null ? 0 : txTransfer.getJsonData().length());
            }
        });
        return r;
    }

    private MoAdjustCommand toCommand(OrderChange change) {
        if (change == null || StringUtils.isBlank(change.getMoCode()) || StringUtils.isBlank(change.getChangeType())) {
            return null;
        }
        MoAdjustTypeEnum type = MoAdjustTypeEnum.of(change.getChangeType());
        if (type == null) {
            log.warn("MO_CHANGE skip unknown changeType={} mo={}", change.getChangeType(), change.getMoCode());
            return null;
        }
        Map<String, Object> payload = change.getPayload() == null ? new HashMap<>() : new HashMap<>(change.getPayload());
        return new MoAdjustCommand()
                .setMoCode(change.getMoCode())
                .setAdjustType(type.name())
                .setSource(MoAdjustSourceEnum.MES.name())
                .setRequestId(StringUtils.isNotBlank(change.getChangeId())
                        ? change.getChangeId()
                        : "MES-" + IdWorker.getIdStr())
                .setOperator("MES")
                .setForce(Boolean.TRUE.equals(change.getForce()))
                .setPayload(payload);
    }
}
