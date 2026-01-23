/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.device.mapper.EquipStateSnapshotMapper;
import com.ourexists.mesedge.device.model.EquipStateSnapshotCountDto;
import com.ourexists.mesedge.device.model.EquipStateSnapshotCountQuery;
import com.ourexists.mesedge.device.model.EquipStateSnapshotDto;
import com.ourexists.mesedge.device.model.EquipStateSnapshotPageQuery;
import com.ourexists.mesedge.device.pojo.EquipStateSnapshot;
import com.ourexists.mesedge.device.pojo.Workshop;
import com.ourexists.mesedge.device.service.EquipStateSnapshotService;
import com.ourexists.mesedge.device.service.WorkshopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipStateSnapshotServiceImpl extends AbstractMyBatisPlusService<EquipStateSnapshotMapper, EquipStateSnapshot>
        implements EquipStateSnapshotService {

    @Autowired
    private WorkshopService workshopService;

    @Override
    public Page<EquipStateSnapshot> selectByPage(EquipStateSnapshotPageQuery dto) {
        LambdaQueryWrapper<EquipStateSnapshot> qw = new LambdaQueryWrapper<EquipStateSnapshot>()
                .eq(StringUtils.hasText(dto.getSn()), EquipStateSnapshot::getSn, dto.getSn())
                .eq(dto.getRunState() != null, EquipStateSnapshot::getRunState, dto.getRunState())
                .eq(dto.getAlarmState() != null, EquipStateSnapshot::getAlarmState, dto.getAlarmState())
                .eq(dto.getOnlineState() != null, EquipStateSnapshot::getOnlineState, dto.getOnlineState())
                .between(dto.getStartDate() != null && dto.getEndDate() != null, EquipStateSnapshot::getTime, dto.getStartDate(), dto.getEndDate())
                .orderByDesc(EquipStateSnapshot::getTime, EquipStateSnapshot::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        this.removeBatchByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(EquipStateSnapshotDto dto) {
        this.save(EquipStateSnapshot.wrap(dto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBatch(List<EquipStateSnapshotDto> dtos) {
        this.saveBatch(EquipStateSnapshot.wrap(dtos));
    }

    @Override
    public List<EquipStateSnapshotCountDto> countNumByTime(EquipStateSnapshotCountQuery dto) {
        if (StringUtils.hasText(dto.getWorkshopCode())) {
            List<String> workShopCodes = new ArrayList<>();
            workShopCodes.add(dto.getWorkshopCode());
            if (dto.getNeedWorkshopCascade()) {
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
            } else {
                dto.setWorkshopCodes(workShopCodes);
            }
        }
        if (dto.getCountType() != null && dto.getCountType() == 1) {
            return this.baseMapper.countNumByTimeDay(dto);
        }
        return this.baseMapper.countNumByTime(dto);
    }
}
