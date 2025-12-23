/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.device.mapper.WorkshopMapper;
import com.ourexists.mesedge.device.model.WorkshopDto;
import com.ourexists.mesedge.device.model.WorkshopPageQuery;
import com.ourexists.mesedge.device.pojo.Device;
import com.ourexists.mesedge.device.pojo.Workshop;
import com.ourexists.mesedge.device.service.DeviceService;
import com.ourexists.mesedge.device.service.WorkshopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkshopServiceImpl extends AbstractMyBatisPlusService<WorkshopMapper, Workshop> implements WorkshopService {

    @Autowired
    private DeviceService deviceService;

    @Override
    public Page<Workshop> selectByPage(WorkshopPageQuery dto) {
        LambdaQueryWrapper<Workshop> qw = new LambdaQueryWrapper<Workshop>()
                .orderByDesc(Workshop::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    public void addOrUpdate(WorkshopDto dto) {
        saveOrUpdate(Workshop.wrap(dto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        this.removeBatchByIds(ids);
        deviceService.remove(new LambdaQueryWrapper<Device>().in(Device::getDgId, ids));
    }
}
