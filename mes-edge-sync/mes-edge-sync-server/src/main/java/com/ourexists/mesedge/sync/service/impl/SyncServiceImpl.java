/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.sync.enums.SyncStatusEnum;
import com.ourexists.mesedge.sync.mapper.SyncMapper;
import com.ourexists.mesedge.sync.model.query.SyncPageQuery;
import com.ourexists.mesedge.sync.pojo.Sync;
import com.ourexists.mesedge.sync.pojo.SyncResource;
import com.ourexists.mesedge.sync.service.SyncResourceService;
import com.ourexists.mesedge.sync.service.SyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
public class SyncServiceImpl extends AbstractMyBatisPlusService<SyncMapper, Sync> implements SyncService {

    @Autowired
    private SyncResourceService syncResourceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String id, SyncStatusEnum syncStatusEnum) {
        this.update(new LambdaUpdateWrapper<Sync>().set(Sync::getStatus, syncStatusEnum.name()).eq(Sync::getId, id));
    }

    @Override
    public Page<Sync> selectByPage(SyncPageQuery dto) {
        LambdaQueryWrapper<Sync> wrapper = new LambdaQueryWrapper<Sync>()
                .eq(!StringUtils.isEmpty(dto.getSyncTx()), Sync::getSyncTx, dto.getSyncTx())
                .eq(!StringUtils.isEmpty(dto.getStatus()), Sync::getStatus, dto.getStatus())
                .ge(dto.getCreateStartDate() != null, Sync::getCreatedTime, dto.getCreateStartDate())
                .le(dto.getCreateEndDate() != null, Sync::getCreatedTime, dto.getCreateEndDate())
                .orderByDesc(Sync::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), wrapper);
    }

    @Override
    public Sync selectLastSync(String syncTx) {
        return getOne(new LambdaQueryWrapper<Sync>()
                .eq(Sync::getSyncTx, syncTx)
                .orderByDesc(Sync::getId)
                .last("limit 1"));
    }

    @Override
    public void clearHistory(Date date) {
        this.remove(new LambdaUpdateWrapper<Sync>().le(Sync::getCreatedTime, date));
        syncResourceService.remove(new LambdaUpdateWrapper<SyncResource>().le(SyncResource::getCreatedTime, date));
    }
}
