package com.ourexists.mesedge.portal.sync;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.era.txflow.TxStore;
import com.ourexists.era.txflow.model.Tx;
import com.ourexists.era.txflow.model.TxBranch;
import com.ourexists.mesedge.sync.feign.SyncFeign;
import com.ourexists.mesedge.sync.model.SyncResourceVo;
import com.ourexists.mesedge.sync.model.SyncVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class SyncTxStore implements TxStore {

    @Autowired
    private SyncFeign syncFeign;

    @Override
    public Tx lastTx(String txName) {
        try {
            SyncVo syncVo = RemoteHandleUtils.getDataFormResponse(syncFeign.selectLastSyncByTx(txName));
            return SyncBeanUtils.wrapTx(syncVo);
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public void txStart(Tx tx) {
        try {
            RemoteHandleUtils.getDataFormResponse(syncFeign.addOrUpdate(SyncBeanUtils.covertTx(tx)));
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public void txError(Tx tx) {
        try {
            RemoteHandleUtils.getDataFormResponse(syncFeign.changeStatus(tx.getId(), tx.getStatus()));
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public void txEnd(Tx tx) {
        try {
            RemoteHandleUtils.getDataFormResponse(syncFeign.changeStatus(tx.getId(), tx.getStatus()));
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public TxBranch lastBranch(String txId) {
        try {
            SyncResourceVo syncResourceVo = RemoteHandleUtils.getDataFormResponse(syncFeign.lastSyncBranch(txId));
            return SyncBeanUtils.wrapBranch(syncResourceVo);
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public void branchStart(TxBranch txBranch) {
        try {
            RemoteHandleUtils.getDataFormResponse(syncFeign.addOrUpdateResource(SyncBeanUtils.covertBranch(txBranch)));
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public void branchError(TxBranch txBranch) {
        try {
            RemoteHandleUtils.getDataFormResponse(syncFeign.addOrUpdateResource(SyncBeanUtils.covertBranch(txBranch)));
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public void branchEnd(TxBranch txBranch) {
        try {
            RemoteHandleUtils.getDataFormResponse(syncFeign.addOrUpdateResource(SyncBeanUtils.covertBranch(txBranch)));
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }

    }

    @Override
    public void clearTx(String txName) {

    }

    @Override
    public void clearAll() {
    }

    @Override
    public void clearHistory(Date date) {
        syncFeign.clearHistory(date);
    }


}
