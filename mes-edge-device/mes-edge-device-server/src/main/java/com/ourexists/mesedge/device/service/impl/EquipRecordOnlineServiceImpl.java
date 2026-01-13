/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.device.mapper.EquipRecordOnlineMapper;
import com.ourexists.mesedge.device.model.EquipRecordOnlineDto;
import com.ourexists.mesedge.device.model.EquipRecordOnlinePageQuery;
import com.ourexists.mesedge.device.pojo.Device;
import com.ourexists.mesedge.device.pojo.EquipRecordOnline;
import com.ourexists.mesedge.device.service.DeviceService;
import com.ourexists.mesedge.device.service.EquipRecordOnlineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class EquipRecordOnlineServiceImpl extends AbstractMyBatisPlusService<EquipRecordOnlineMapper, EquipRecordOnline>
        implements EquipRecordOnlineService {

    @Autowired
    private DeviceService deviceService;

    @Override
    public Page<EquipRecordOnline> selectByPage(EquipRecordOnlinePageQuery dto) {
        LambdaQueryWrapper<EquipRecordOnline> qw = new LambdaQueryWrapper<EquipRecordOnline>()
                .eq(StringUtils.hasText(dto.getSn()), EquipRecordOnline::getSn, dto.getSn())
                .eq(dto.getState() != null, EquipRecordOnline::getState, dto.getState())
                .ge(dto.getStartDate() != null, EquipRecordOnline::getStartTime, dto.getStartDate())
                .le(dto.getEndDate() != null, EquipRecordOnline::getStartTime, dto.getEndDate())
                .orderByDesc(EquipRecordOnline::getId);
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
    public void add(EquipRecordOnlineDto dto) {
        dto.setId(null);
        EquipRecordOnline current = EquipRecordOnline.wrap(dto);
        EquipRecordOnline last = this.getOne(new LambdaQueryWrapper<EquipRecordOnline>()
                .eq(EquipRecordOnline::getSn, dto.getSn())
                .orderByDesc(EquipRecordOnline::getStartTime, EquipRecordOnline::getId)
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
