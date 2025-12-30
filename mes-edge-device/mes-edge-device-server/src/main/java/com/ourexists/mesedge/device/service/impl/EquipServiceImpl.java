/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.device.mapper.EquipMapper;
import com.ourexists.mesedge.device.model.EquipPageQuery;
import com.ourexists.mesedge.device.pojo.Equip;
import com.ourexists.mesedge.device.pojo.Workshop;
import com.ourexists.mesedge.device.service.EquipService;
import com.ourexists.mesedge.device.service.WorkshopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class EquipServiceImpl extends AbstractMyBatisPlusService<EquipMapper, Equip> implements EquipService {

    @Autowired
    private WorkshopService workshopService;

    @Override
    public Page<Equip> selectByPage(EquipPageQuery dto) {
        List<String> workShopCodes = new ArrayList<>();
        if (dto.getNeedWorkshopCascade() && StringUtils.hasText(dto.getWorkshopCode())) {
            workShopCodes.add(dto.getWorkshopCode());
            List<Workshop> children = workshopService.queryChildBySelfCode(dto.getWorkshopCode());
            workShopCodes.addAll(children.stream().map(Workshop::getSelfCode).toList());
            dto.setWorkshopCode(null);
        }
        LambdaQueryWrapper<Equip> qw = new LambdaQueryWrapper<Equip>().eq(StringUtils.hasText(dto.getWorkshopCode()), Equip::getWorkshopCode, dto.getWorkshopCode()).eq(StringUtils.hasText(dto.getSelfCode()), Equip::getSelfCode, dto.getSelfCode()).eq(dto.getType() != null, Equip::getType, dto.getType()).in(!CollectionUtils.isEmpty(workShopCodes), Equip::getWorkshopCode, workShopCodes).like(StringUtils.hasText(dto.getName()), Equip::getName, dto.getName()).orderByDesc(Equip::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }
}
