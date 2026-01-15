/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.device.mapper.EquipRecordAlarmMapper;
import com.ourexists.mesedge.device.model.EquipRecordAlarmDto;
import com.ourexists.mesedge.device.model.EquipRecordAlarmPageQuery;
import com.ourexists.mesedge.device.pojo.Device;
import com.ourexists.mesedge.device.pojo.EquipRecordAlarm;
import com.ourexists.mesedge.device.service.DeviceService;
import com.ourexists.mesedge.device.service.EquipRecordAlarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class EquipRecordAlarmServiceImpl extends AbstractMyBatisPlusService<EquipRecordAlarmMapper, EquipRecordAlarm>
        implements EquipRecordAlarmService {

    @Autowired
    private DeviceService deviceService;

    @Override
    public Page<EquipRecordAlarm> selectByPage(EquipRecordAlarmPageQuery dto) {
        LambdaQueryWrapper<EquipRecordAlarm> qw = new LambdaQueryWrapper<EquipRecordAlarm>()
                .eq(StringUtils.hasText(dto.getSn()), EquipRecordAlarm::getSn, dto.getSn())
                .eq(dto.getState() != null, EquipRecordAlarm::getState, dto.getState())
                .and(dto.getStartDate() != null && dto.getEndDate() != null, wrapper -> {
                    wrapper
                            .between(EquipRecordAlarm::getStartTime, dto.getStartDate(), dto.getEndDate())
                            .or()
                            .between(EquipRecordAlarm::getEndTime, dto.getStartDate(), dto.getEndDate());
                })
                .orderByDesc(EquipRecordAlarm::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        this.removeBatchByIds(ids);
        deviceService.remove(new LambdaQueryWrapper<Device>().in(Device::getDgId, ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(EquipRecordAlarmDto dto) {
        dto.setId(null);
        EquipRecordAlarm current = EquipRecordAlarm.wrap(dto);
        EquipRecordAlarm last = this.getOne(new LambdaQueryWrapper<EquipRecordAlarm>()
                .eq(EquipRecordAlarm::getSn, dto.getSn())
                .orderByDesc(EquipRecordAlarm::getStartTime, EquipRecordAlarm::getId)
                .last("limit 1")
        );
        if (last != null) {
            //处理中间服务中断
            if (!last.getState().equals(current.getState())) {
                last.setEndTime(current.getStartTime());
                this.updateById(last);
                this.save(current);
            }
        } else {
            this.save(current);
        }
    }
}
