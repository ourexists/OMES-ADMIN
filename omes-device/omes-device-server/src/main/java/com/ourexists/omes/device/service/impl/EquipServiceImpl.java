/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.device.mapper.EquipMapper;
import com.ourexists.omes.device.model.EquipPageQuery;
import com.ourexists.omes.device.pojo.Equip;
import com.ourexists.omes.device.pojo.Workshop;
import com.ourexists.omes.device.service.EquipService;
import com.ourexists.omes.device.service.WorkshopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipServiceImpl extends AbstractMyBatisPlusService<EquipMapper, Equip> implements EquipService {

    @Autowired
    private WorkshopService workshopService;

    @Override
    public Page<Equip> selectByPage(EquipPageQuery dto) {
        if (dto.getNeedWorkshopCascade() && StringUtils.hasText(dto.getWorkshopCode())) {
            List<String> workShopCodes = new ArrayList<>();
            workShopCodes.add(dto.getWorkshopCode());
            List<Workshop> children = workshopService.queryChildBySelfCode(dto.getWorkshopCode());
            workShopCodes.addAll(children.stream().map(Workshop::getSelfCode).toList());
            dto.setWorkshopCode(null);
            if (!CollectionUtils.isEmpty(dto.getWorkshopCodes())) {
                List<String> ty = dto.getWorkshopCodes();
                List<String> intersection = ty.stream()
                        .filter(workShopCodes::contains)
                        .collect(Collectors.toList());
                dto.setWorkshopCodes(intersection);
            } else {
                dto.setWorkshopCodes(workShopCodes);
            }
        }
        LambdaQueryWrapper<Equip> qw = new LambdaQueryWrapper<Equip>()
                .eq(StringUtils.hasText(dto.getWorkshopCode()), Equip::getWorkshopCode, dto.getWorkshopCode())
                .eq(StringUtils.hasText(dto.getSelfCode()), Equip::getSelfCode, dto.getSelfCode())
                .eq(StringUtils.hasText(dto.getType()), Equip::getType, dto.getType())
                .in(!CollectionUtils.isEmpty(dto.getWorkshopCodes()), Equip::getWorkshopCode, dto.getWorkshopCodes())
                .like(StringUtils.hasText(dto.getName()), Equip::getName, dto.getName())
                .isNotNull(dto.getExistHealth() != null && dto.getExistHealth(), Equip::getHealthTemplateId)
                .inSql(StringUtils.hasText(dto.getGwId()), Equip::getId, "select equip_id from r_gw_binding where gw_id=" + dto.getGwId())
                .orderByDesc(Equip::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    public Equip getBySelfCode(String selfCode) {
        if (selfCode == null || selfCode.isEmpty()) return null;
        return getOne(new LambdaQueryWrapper<Equip>().eq(Equip::getSelfCode, selfCode).last("limit 1"));
    }

    @Override
    public List<Equip> listByHealthTemplateId(String healthTemplateId) {
        if (healthTemplateId == null || healthTemplateId.isEmpty()) return new ArrayList<>();
        return list(new LambdaQueryWrapper<Equip>().eq(Equip::getHealthTemplateId, healthTemplateId));
    }
}
