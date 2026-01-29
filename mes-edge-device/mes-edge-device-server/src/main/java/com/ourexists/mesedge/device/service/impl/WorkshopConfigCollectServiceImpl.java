/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.device.mapper.WorkshopConfigCollectMapper;
import com.ourexists.mesedge.device.model.WorkshopConfigCollectDto;
import com.ourexists.mesedge.device.pojo.WorkshopConfigCollect;
import com.ourexists.mesedge.device.service.WorkshopConfigCollectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkshopConfigCollectServiceImpl extends AbstractMyBatisPlusService<WorkshopConfigCollectMapper, WorkshopConfigCollect>
        implements WorkshopConfigCollectService {


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdate(WorkshopConfigCollectDto dto) {
        this.saveOrUpdate(WorkshopConfigCollect.wrap(dto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        this.removeBatchByIds(ids);
    }

    @Override
    public List<WorkshopConfigCollect> queryByWorkshop(List<String> workshopIds) {
        return this.list(new LambdaQueryWrapper<WorkshopConfigCollect>()
                .in(WorkshopConfigCollect::getWorkshopId, workshopIds)
                .orderByDesc(WorkshopConfigCollect::getWorkshopId)
        );
    }

    @Override
    public WorkshopConfigCollect queryByWorkshop(String workshopId) {
        return this.getOne(new LambdaQueryWrapper<WorkshopConfigCollect>()
                .eq(WorkshopConfigCollect::getWorkshopId, workshopId));
    }

    @Override
    public List<WorkshopConfigCollect> queryAllConfigCollect() {
        return this.list(new LambdaQueryWrapper<WorkshopConfigCollect>().orderByDesc(WorkshopConfigCollect::getWorkshopId));
    }
}
