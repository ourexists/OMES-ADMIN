package com.ourexists.mesedge.portal.sync;

import cn.hutool.core.bean.BeanUtil;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.txflow.model.Tx;
import com.ourexists.era.txflow.model.TxBranch;
import com.ourexists.mesedge.sync.model.SyncDto;
import com.ourexists.mesedge.sync.model.SyncResourceDto;

import java.util.ArrayList;
import java.util.List;

public class SyncBeanUtils {

    private SyncBeanUtils() {
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

    public static <T extends SyncDto> List<Tx> wrapTx(List<T> sources) {
        List<Tx> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrapTx(source)));
        }
        return targets;
    }

    public static SyncDto covertTx(Tx source) {
        if (source == null) {
            return null;
        }
        SyncDto target = new SyncDto();
        BeanUtil.copyProperties(source, target);
        target.setSyncTx(source.getTx());
        return target;
    }

    public List<SyncDto> covertTx(List<Tx> sources) {
        List<SyncDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covertTx(source)));
        }
        return targets;
    }


    public static <T extends SyncResourceDto> TxBranch wrapBranch(T source) {
        if (source == null) {
            return null;
        }
        TxBranch target = new TxBranch();
        BeanUtil.copyProperties(source, target);
        target.setTxId(source.getSyncId());
        return target;
    }

    public static <T extends SyncResourceDto> List<TxBranch> wrapBranch(List<T> sources) {
        List<TxBranch> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrapBranch(source)));
        }
        return targets;
    }


    public static SyncResourceDto covertBranch(TxBranch source) {
        if (source == null) {
            return null;
        }
        SyncResourceDto target = new SyncResourceDto();
        BeanUtil.copyProperties(source, target);
        target.setSyncId(source.getTxId());
        return target;
    }

    public List<SyncResourceDto> covertBranch(List<TxBranch> sources) {
        List<SyncResourceDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covertBranch(source)));
        }
        return targets;
    }
}
