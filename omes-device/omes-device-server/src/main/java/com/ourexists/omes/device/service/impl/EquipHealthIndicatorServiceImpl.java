/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.core.utils.DateUtil;
import com.ourexists.omes.device.mapper.EquipHealthIndicatorMapper;
import com.ourexists.omes.device.model.EquipHealthIndicatorDto;
import com.ourexists.omes.device.model.EquipHealthIndicatorPageQuery;
import com.ourexists.omes.device.pojo.EquipHealthIndicator;
import com.ourexists.omes.device.service.EquipHealthIndicatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipHealthIndicatorServiceImpl implements EquipHealthIndicatorService {

    @Autowired
    private EquipHealthIndicatorMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(EquipHealthIndicatorDto dto) {
        LambdaQueryWrapper<EquipHealthIndicator> qw = new LambdaQueryWrapper<EquipHealthIndicator>()
                .eq(EquipHealthIndicator::getSn, dto.getSn())
                .eq(EquipHealthIndicator::getStatTime, dto.getStatTime());
        EquipHealthIndicator existing = mapper.selectOne(qw);
        EquipHealthIndicator entity = EquipHealthIndicator.fromDto(dto);
        if (existing != null) {
            entity.setId(existing.getId());
            mapper.updateById(entity);
        } else {
            mapper.insert(entity);
        }
    }

    @Override
    public EquipHealthIndicatorDto getLatestBySn(String sn, Date statTime) {
        LambdaQueryWrapper<EquipHealthIndicator> qw = new LambdaQueryWrapper<EquipHealthIndicator>()
                .eq(EquipHealthIndicator::getSn, sn)
                .eq(EquipHealthIndicator::getStatTime, statTime)
                .last("limit 1");
        EquipHealthIndicator one = mapper.selectOne(qw);
        return one != null ? EquipHealthIndicator.toDto(one) : null;
    }

    @Override
    public EquipHealthIndicatorDto getLatestBySn(String sn) {
        LambdaQueryWrapper<EquipHealthIndicator> qw = new LambdaQueryWrapper<EquipHealthIndicator>()
                .eq(EquipHealthIndicator::getSn, sn)
                .orderByDesc(EquipHealthIndicator::getStatTime)
                .last("limit 1");
        EquipHealthIndicator one = mapper.selectOne(qw);
        return one != null ? EquipHealthIndicator.toDto(one) : null;
    }

    @Override
    public List<EquipHealthIndicatorDto> listByStatTime(EquipHealthIndicatorPageQuery pageQuery) {
        LambdaQueryWrapper<EquipHealthIndicator> qw = new LambdaQueryWrapper<EquipHealthIndicator>()
                .between(pageQuery.getStatTime() != null,
                        EquipHealthIndicator::getStatTime,
                        DateUtil.getDayStart(pageQuery.getStatTime()),
                        DateUtil.getDayEnd(pageQuery.getStatTime()));
        return mapper.selectList(qw).stream()
                .map(EquipHealthIndicator::toDto)
                .collect(Collectors.toList());
    }
}
