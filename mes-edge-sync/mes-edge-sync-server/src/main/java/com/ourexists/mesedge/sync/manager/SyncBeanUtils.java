package com.ourexists.mesedge.sync.manager;

import cn.hutool.core.bean.BeanUtil;
import com.ourexists.era.txflow.model.Tx;
import com.ourexists.era.txflow.model.TxBranch;
import com.ourexists.mesedge.sync.model.SyncDto;
import com.ourexists.mesedge.sync.pojo.Sync;
import com.ourexists.mesedge.sync.pojo.SyncResource;

public class SyncBeanUtils {

    private SyncBeanUtils() {
    }

    public static Tx wrapTx(Sync source) {
        if (source == null) {
            return null;
        }
        Tx target = new Tx();
        BeanUtil.copyProperties(source, target);
        target.setTx(source.getSyncTx());
        return target;
    }

    public static <T extends SyncDto> Tx wrapTx(T source) {
        if (source == null) {
            return null;
        }
        Tx target = new Tx();
        BeanUtil.copyProperties(source, target);
        target.setTx(source.getSyncTx());
        return target;
    }

    public static Sync covertTx(Tx source) {
        if (source == null) {
            return null;
        }
        Sync target = new Sync();
        BeanUtil.copyProperties(source, target);
        target.setSyncTx(source.getTx());
        return target;
    }


    public static TxBranch wrapBranch(SyncResource source) {
        if (source == null) {
            return null;
        }
        TxBranch target = new TxBranch();
        BeanUtil.copyProperties(source, target);
        target.setTxId(source.getSyncId());
        return target;
    }

    public static SyncResource covertBranch(TxBranch source) {
        if (source == null) {
            return null;
        }
        SyncResource target = new SyncResource();
        BeanUtil.copyProperties(source, target);
        target.setSyncId(source.getTxId());
        return target;
    }

}
