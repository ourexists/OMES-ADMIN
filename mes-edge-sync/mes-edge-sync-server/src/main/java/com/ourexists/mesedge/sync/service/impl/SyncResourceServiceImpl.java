/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.sync.enums.SyncTxEnum;
import com.ourexists.mesedge.sync.mapper.SyncResourceMapper;
import com.ourexists.mesedge.sync.pojo.SyncResource;
import com.ourexists.mesedge.sync.service.SyncResourceService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SyncResourceServiceImpl extends AbstractMyBatisPlusService<SyncResourceMapper, SyncResource> implements SyncResourceService {

    @Override
    public SyncResource getLastFlow(String syncId) {
        return this.getOne(new LambdaQueryWrapper<SyncResource>().eq(SyncResource::getSyncId, syncId).orderByDesc(SyncResource::getId).last("limit 1"));
    }

    @Override
    public List<SyncResource> selectBySyncId(String syncId) {
        return this.list(new LambdaUpdateWrapper<SyncResource>().eq(SyncResource::getSyncId, syncId).orderByDesc(SyncResource::getId));
    }

    @Override
    public List<SyncResource> selectBySyncId(List<String> syncIds) {
        return this.list(new LambdaUpdateWrapper<SyncResource>().in(SyncResource::getSyncId, syncIds).orderByDesc(SyncResource::getId));
    }

    @Override
    public boolean existSync(SyncTxEnum syncTxEnum, String reqData) {
        Long c = this.baseMapper.existSync(syncTxEnum.name(), reqData);
        return c > 0;
    }
}
