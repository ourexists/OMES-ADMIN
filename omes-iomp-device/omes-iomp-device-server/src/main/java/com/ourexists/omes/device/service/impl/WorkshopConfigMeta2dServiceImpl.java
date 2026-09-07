/*
 * Copyright (c) 2026.
 */
package com.ourexists.omes.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.device.mapper.WorkshopConfigMeta2dMapper;
import com.ourexists.omes.device.model.WorkshopConfigMeta2dDto;
import com.ourexists.omes.device.pojo.WorkshopConfigMeta2d;
import com.ourexists.omes.device.service.WorkshopConfigMeta2dService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkshopConfigMeta2dServiceImpl
        extends AbstractMyBatisPlusService<WorkshopConfigMeta2dMapper, WorkshopConfigMeta2d>
        implements WorkshopConfigMeta2dService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdate(WorkshopConfigMeta2dDto dto) {
        // 以 workshopId 为唯一约束（覆盖式保存）
        this.remove(new LambdaQueryWrapper<WorkshopConfigMeta2d>()
                .eq(WorkshopConfigMeta2d::getWorkshopId, dto.getWorkshopId()));
        this.saveOrUpdate(WorkshopConfigMeta2d.wrap(dto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        this.removeBatchByIds(ids);
    }

    @Override
    public List<WorkshopConfigMeta2d> queryByWorkshop(List<String> workshopIds) {
        return this.list(new LambdaQueryWrapper<WorkshopConfigMeta2d>()
                .in(WorkshopConfigMeta2d::getWorkshopId, workshopIds)
                .orderByDesc(WorkshopConfigMeta2d::getId)
        );
    }

    @Override
    public WorkshopConfigMeta2d queryByWorkshop(String workshopId) {
        return this.getOne(new LambdaQueryWrapper<WorkshopConfigMeta2d>()
                .eq(WorkshopConfigMeta2d::getWorkshopId, workshopId));
    }
}

