/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.device.mapper.WorkshopConfigScadaMapper;
import com.ourexists.mesedge.device.model.WorkshopConfigScadaDto;
import com.ourexists.mesedge.device.pojo.WorkshopConfigScada;
import com.ourexists.mesedge.device.service.WorkshopConfigScadaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkshopConfigScadaServiceImpl extends AbstractMyBatisPlusService<WorkshopConfigScadaMapper, WorkshopConfigScada> implements WorkshopConfigScadaService {


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdate(WorkshopConfigScadaDto dto) {
        this.remove(new LambdaQueryWrapper<WorkshopConfigScada>()
                .eq(WorkshopConfigScada::getWorkshopId, dto.getWorkshopId()));
        this.saveOrUpdate(WorkshopConfigScada.wrap(dto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        this.removeBatchByIds(ids);
    }

    @Override
    public List<WorkshopConfigScada> queryByWorkshop(List<String> workshopIds) {
        return this.list(new LambdaQueryWrapper<WorkshopConfigScada>()
                .in(WorkshopConfigScada::getWorkshopId, workshopIds)
                .orderByDesc(WorkshopConfigScada::getId)
        );
    }

    @Override
    public WorkshopConfigScada queryByWorkshop(String workshopId) {
        return this.getOne(new LambdaQueryWrapper<WorkshopConfigScada>()
                .eq(WorkshopConfigScada::getWorkshopId, workshopId));
    }
}
