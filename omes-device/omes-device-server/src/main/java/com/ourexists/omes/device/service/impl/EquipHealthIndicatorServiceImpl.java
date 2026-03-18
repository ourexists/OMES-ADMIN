/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.core.utils.DateUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.device.mapper.EquipHealthIndicatorMapper;
import com.ourexists.omes.device.model.EquipHealthIndicatorDto;
import com.ourexists.omes.device.model.EquipHealthIndicatorPageQuery;
import com.ourexists.omes.device.pojo.EquipHealthIndicator;
import com.ourexists.omes.device.service.EquipHealthIndicatorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipHealthIndicatorServiceImpl extends AbstractMyBatisPlusService<EquipHealthIndicatorMapper, EquipHealthIndicator>
        implements EquipHealthIndicatorService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(EquipHealthIndicatorDto dto) {
        this.saveOrUpdate(EquipHealthIndicator.fromDto(dto));
    }

    @Override
    public EquipHealthIndicatorDto getLatestBySn(String sn, Date statTime) {
        EquipHealthIndicator one = this.getOne(
                new LambdaQueryWrapper<EquipHealthIndicator>()
                        .eq(EquipHealthIndicator::getSn, sn)
                        .eq(EquipHealthIndicator::getStatTime, statTime));
        return one != null ? EquipHealthIndicator.toDto(one) : null;
    }

    @Override
    public EquipHealthIndicatorDto getLatestBySn(String sn) {
        EquipHealthIndicator one = this.getOne(new LambdaQueryWrapper<EquipHealthIndicator>()
                .eq(EquipHealthIndicator::getSn, sn)
                .orderByDesc(EquipHealthIndicator::getStatTime));
        return one != null ? EquipHealthIndicator.toDto(one) : null;
    }

    @Override
    public List<EquipHealthIndicatorDto> listByStatTime(EquipHealthIndicatorPageQuery pageQuery) {
        LambdaQueryWrapper<EquipHealthIndicator> qw = new LambdaQueryWrapper<EquipHealthIndicator>()
                .between(pageQuery.getStatTime() != null,
                        EquipHealthIndicator::getStatTime,
                        DateUtil.getDayStart(pageQuery.getStatTime()),
                        DateUtil.getDayEnd(pageQuery.getStatTime()));
        return this.list(qw).stream()
                .map(EquipHealthIndicator::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBatch(List<EquipHealthIndicatorDto> list) {
        for (EquipHealthIndicatorDto dto : list) {
            saveOrUpdate(dto);
        }
    }
}
