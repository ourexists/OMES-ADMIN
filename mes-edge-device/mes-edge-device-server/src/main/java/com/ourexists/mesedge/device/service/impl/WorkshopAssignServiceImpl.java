/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.device.mapper.EquipMapper;
import com.ourexists.mesedge.device.mapper.WorkshopAssignMapper;
import com.ourexists.mesedge.device.model.WorkshopAssignBatchDto;
import com.ourexists.mesedge.device.pojo.Equip;
import com.ourexists.mesedge.device.pojo.WorkshopAssign;
import com.ourexists.mesedge.device.service.WorkshopAssignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class WorkshopAssignServiceImpl extends AbstractMyBatisPlusService<WorkshopAssignMapper, WorkshopAssign> implements WorkshopAssignService {

    @Autowired
    private EquipMapper equipMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(WorkshopAssignBatchDto workshopAssignBatchDto) {
        List<WorkshopAssign> assigns = new ArrayList<>();
        for (String s : workshopAssignBatchDto.getWorkshopCodes()) {
            WorkshopAssign workshopAssign = new WorkshopAssign();
            workshopAssign.setAssignId(workshopAssignBatchDto.getAssignId());
            workshopAssign.setWorkshopCode(s);
            assigns.add(workshopAssign);
        }
        this.remove(new LambdaQueryWrapper<WorkshopAssign>().eq(WorkshopAssign::getAssignId, workshopAssignBatchDto.getAssignId()));
        this.saveBatch(assigns);
    }

    @Override
    public List<WorkshopAssign> selectWorkshopAssignByEquipId(String equipId) {
        Equip equip = equipMapper.selectById(equipId);
        if (StringUtils.hasText(equip.getWorkshopCode())) {
            LambdaQueryWrapper<WorkshopAssign> queryWrapper = new LambdaQueryWrapper<WorkshopAssign>()
                    .eq(WorkshopAssign::getWorkshopCode, equip.getWorkshopCode());
            return this.list(queryWrapper);
        }
        return List.of();
    }
}
