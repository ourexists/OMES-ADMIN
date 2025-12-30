/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.device.core.EquipRealtime;
import com.ourexists.mesedge.device.core.EquipRealtimeManager;
import com.ourexists.mesedge.device.mapper.EquipRunRecordMapper;
import com.ourexists.mesedge.device.model.EquipRunRecordDto;
import com.ourexists.mesedge.device.model.EquipRunRecordPageQuery;
import com.ourexists.mesedge.device.pojo.Device;
import com.ourexists.mesedge.device.pojo.EquipRunRecord;
import com.ourexists.mesedge.device.service.DeviceService;
import com.ourexists.mesedge.device.service.EquipRunRecordService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EquipRunRecordServiceImpl extends AbstractMyBatisPlusService<EquipRunRecordMapper, EquipRunRecord> implements EquipRunRecordService {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private EquipRealtimeManager realtimeManager;

    @Override
    public Page<EquipRunRecord> selectByPage(EquipRunRecordPageQuery dto) {
        LambdaQueryWrapper<EquipRunRecord> qw = new LambdaQueryWrapper<EquipRunRecord>().ge(dto.getRunStartTimeStart() != null, EquipRunRecord::getRunStartTime, dto.getRunStartTimeStart()).le(dto.getRunEndTimeEnd() != null, EquipRunRecord::getRunEndTime, dto.getRunEndTimeEnd()).orderByDesc(EquipRunRecord::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdate(EquipRunRecordDto dto) {
        EquipRealtime equipRealtime = new EquipRealtime();
        BeanUtils.copyProperties(dto, equipRealtime);
        saveOrUpdate(EquipRunRecord.wrap(dto));
        realtimeManager.addOrUpdate(UserContext.getTenant().getTenantId(), equipRealtime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        this.removeBatchByIds(ids);
        deviceService.remove(new LambdaQueryWrapper<Device>().in(Device::getDgId, ids));
    }
}
