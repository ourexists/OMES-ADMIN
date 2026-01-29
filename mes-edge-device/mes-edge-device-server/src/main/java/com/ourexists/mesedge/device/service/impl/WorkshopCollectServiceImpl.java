/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.device.mapper.WorkshopCollectMapper;
import com.ourexists.mesedge.device.model.WorkshopCollectDto;
import com.ourexists.mesedge.device.model.WorkshopCollectPageQuery;
import com.ourexists.mesedge.device.pojo.WorkshopCollect;
import com.ourexists.mesedge.device.service.WorkshopCollectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class WorkshopCollectServiceImpl extends AbstractMyBatisPlusService<WorkshopCollectMapper, WorkshopCollect>
        implements WorkshopCollectService {

    @Override
    public Page<WorkshopCollect> selectByPage(WorkshopCollectPageQuery dto) {
        LambdaQueryWrapper<WorkshopCollect> qw = new LambdaQueryWrapper<WorkshopCollect>()
                .eq(StringUtils.hasText(dto.getWorkshopId()), WorkshopCollect::getWorkshopId, dto.getWorkshopId())
                .between(dto.getStartDate() != null && dto.getEndDate() != null, WorkshopCollect::getTime, dto.getStartDate(), dto.getEndDate())
                .orderByAsc(WorkshopCollect::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    public void addOrUpdate(WorkshopCollectDto dto) {
        saveOrUpdate(WorkshopCollect.wrap(dto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        this.removeBatchByIds(ids);
    }

    @Override
    public List<WorkshopCollect> queryByWorkshop(List<String> workshopIds) {
        return this.list(new LambdaUpdateWrapper<WorkshopCollect>().in(WorkshopCollect::getWorkshopId, workshopIds));
    }

    @Override
    public WorkshopCollect queryByWorkshop(String workshopId) {
        return this.getOne(new LambdaUpdateWrapper<WorkshopCollect>().eq(WorkshopCollect::getWorkshopId, workshopId));
    }
}
