package com.ourexists.mesedge.sync.manager;

import com.ourexists.era.txflow.TxStore;
import com.ourexists.era.txflow.model.Tx;
import com.ourexists.era.txflow.model.TxBranch;
import com.ourexists.mesedge.sync.service.SyncResourceService;
import com.ourexists.mesedge.sync.service.SyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class SyncTxStore implements TxStore {

    @Autowired
    private SyncService syncService;

    @Autowired
    private SyncResourceService syncResourceService;

    @Override
    public Tx lastTx(String txName) {
        return SyncBeanUtils.wrapTx(syncService.selectLastSync(txName));
    }

    @Override
    public void txStart(Tx tx) {
        syncService.saveOrUpdate(SyncBeanUtils.covertTx(tx));
    }

    @Override
    public void txError(Tx tx) {
        syncService.updateStatus(tx.getId(), tx.getStatus());
    }

    @Override
    public void txEnd(Tx tx) {
        syncService.updateStatus(tx.getId(), tx.getStatus());
    }

    @Override
    public TxBranch lastBranch(String txId) {
        return SyncBeanUtils.wrapBranch(syncResourceService.getLastFlow(txId));
    }

    @Override
    public void branchStart(TxBranch txBranch) {
        syncResourceService.saveOrUpdate(SyncBeanUtils.covertBranch(txBranch));
    }

    @Override
    public void branchError(TxBranch txBranch) {
        syncResourceService.saveOrUpdate(SyncBeanUtils.covertBranch(txBranch));
    }

    @Override
    public void branchEnd(TxBranch txBranch) {
        syncResourceService.saveOrUpdate(SyncBeanUtils.covertBranch(txBranch));
    }

    @Override
    public void clearTx(String txName) {

    }

    @Override
    public void clearAll() {
    }

    @Override
    public void clearHistory(Date date) {
        syncService.clearHistory(date);
    }
}
