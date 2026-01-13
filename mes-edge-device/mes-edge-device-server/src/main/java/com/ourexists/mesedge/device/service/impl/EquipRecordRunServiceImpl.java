/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.device.mapper.EquipRecordRunMapper;
import com.ourexists.mesedge.device.model.EquipRecordRunDto;
import com.ourexists.mesedge.device.model.EquipRecordRunPageQuery;
import com.ourexists.mesedge.device.pojo.Device;
import com.ourexists.mesedge.device.pojo.EquipRecordRun;
import com.ourexists.mesedge.device.service.DeviceService;
import com.ourexists.mesedge.device.service.EquipRecordRunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class EquipRecordRunServiceImpl extends AbstractMyBatisPlusService<EquipRecordRunMapper, EquipRecordRun>
        implements EquipRecordRunService {

    @Autowired
    private DeviceService deviceService;

    @Override
    public Page<EquipRecordRun> selectByPage(EquipRecordRunPageQuery dto) {
        LambdaQueryWrapper<EquipRecordRun> qw = new LambdaQueryWrapper<EquipRecordRun>()
                .eq(StringUtils.hasText(dto.getSn()), EquipRecordRun::getSn, dto.getSn())
                .eq(dto.getState() != null, EquipRecordRun::getState, dto.getState())
                .ge(dto.getStartDate() != null, EquipRecordRun::getStartTime, dto.getStartDate())
                .le(dto.getEndDate() != null, EquipRecordRun::getStartTime, dto.getEndDate())
                .orderByDesc(EquipRecordRun::getId);
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
    public void add(EquipRecordRunDto dto) {
        dto.setId(null);
        EquipRecordRun current = EquipRecordRun.wrap(dto);
        EquipRecordRun last = this.getOne(new LambdaQueryWrapper<EquipRecordRun>()
                .eq(EquipRecordRun::getSn, dto.getSn())
                .orderByDesc(EquipRecordRun::getStartTime, EquipRecordRun::getId)
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
