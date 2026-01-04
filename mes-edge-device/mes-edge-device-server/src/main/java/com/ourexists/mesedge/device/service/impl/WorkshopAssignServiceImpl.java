/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.device.mapper.WorkshopAssignMapper;
import com.ourexists.mesedge.device.model.WorkshopAssignDto;
import com.ourexists.mesedge.device.pojo.WorkshopAssign;
import com.ourexists.mesedge.device.service.WorkshopAssignService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class WorkshopAssignServiceImpl extends AbstractMyBatisPlusService<WorkshopAssignMapper, WorkshopAssign> implements WorkshopAssignService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(WorkshopAssignDto workshopAssignDto) {
        List<WorkshopAssign> assigns = new ArrayList<>();
        for (String s : workshopAssignDto.getWorkshopCodes()) {
            WorkshopAssign workshopAssign = new WorkshopAssign();
            workshopAssign.setAssignId(workshopAssignDto.getAssignId());
            workshopAssign.setWorkshopCode(s);
            assigns.add(workshopAssign);
        }
        this.remove(new LambdaQueryWrapper<WorkshopAssign>().eq(WorkshopAssign::getAssignId, workshopAssignDto.getAssignId()));
        this.saveBatch(assigns);
    }
}
