/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.device.core.EquipRealtime;
import com.ourexists.mesedge.device.core.EquipRealtimeManager;
import com.ourexists.mesedge.device.mapper.EquipRecordRunMapper;
import com.ourexists.mesedge.device.model.EquipRecordCountQuery;
import com.ourexists.mesedge.device.model.EquipRecordRunDto;
import com.ourexists.mesedge.device.model.EquipRecordRunPageQuery;
import com.ourexists.mesedge.device.model.EquipRecordRunVo;
import com.ourexists.mesedge.device.pojo.EquipRecordRun;
import com.ourexists.mesedge.device.service.EquipRecordRunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class EquipRecordRunServiceImpl extends AbstractMyBatisPlusService<EquipRecordRunMapper, EquipRecordRun>
        implements EquipRecordRunService {

    @Autowired
    private EquipRealtimeManager realtimeManager;

    @Override
    public Page<EquipRecordRun> selectByPage(EquipRecordRunPageQuery dto) {
        LambdaQueryWrapper<EquipRecordRun> qw = new LambdaQueryWrapper<EquipRecordRun>()
                .eq(StringUtils.hasText(dto.getSn()), EquipRecordRun::getSn, dto.getSn())
                .eq(dto.getState() != null, EquipRecordRun::getState, dto.getState())
                .and(dto.getStartDate() != null && dto.getEndDate() != null, wrapper -> {
                    wrapper
                            .between(EquipRecordRun::getStartTime, dto.getStartDate(), dto.getEndDate())
                            .or()
                            .between(EquipRecordRun::getEndTime, dto.getStartDate(), dto.getEndDate());
                })
                .orderByDesc(EquipRecordRun::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        this.removeBatchByIds(ids);
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

    @Override
    public List<EquipRecordRunVo> countMerging(EquipRecordCountQuery query) {
        List<EquipRecordRunVo> r = new ArrayList<>();
        EquipRealtime equipRealtime = realtimeManager.get(UserContext.getTenant().getTenantId(), query.getSn());
        Date now = new Date();
        //限制最大查询时间不能大于当前时间
        if (query.getEndDate().after(now)) {
            query.setEndDate(now);
        }
        if (equipRealtime != null && equipRealtime.getRunChangeTime() != null
                && !equipRealtime.getRunChangeTime().after(query.getStartDate())) {
            EquipRecordRunVo e = new EquipRecordRunVo();
            e.setSn(query.getSn());
            e.setStartTime(query.getStartDate());
            e.setEndTime(query.getEndDate());
            e.setState(equipRealtime.getRunState());
            e.setTenantId(equipRealtime.getTenantId());
            r.add(e);
        } else {
            Date queryEndDate;
            if (equipRealtime != null && equipRealtime.getRunChangeTime() != null &&
                    equipRealtime.getRunChangeTime().before(query.getEndDate())) {
                EquipRecordRunVo e = new EquipRecordRunVo();
                e.setSn(query.getSn());
                e.setStartTime(equipRealtime.getRunChangeTime());
                e.setEndTime(query.getEndDate());
                e.setState(equipRealtime.getRunState());
                e.setTenantId(equipRealtime.getTenantId());
                r.add(e);
                queryEndDate = equipRealtime.getRunChangeTime();
            } else {
                queryEndDate = query.getEndDate();
            }
            LambdaQueryWrapper<EquipRecordRun> qw = new LambdaQueryWrapper<EquipRecordRun>()
                    .eq(EquipRecordRun::getSn, query.getSn())
                    .and(wrapper -> {
                        wrapper
                                .between(EquipRecordRun::getStartTime, query.getStartDate(), queryEndDate)
                                .or()
                                .between(EquipRecordRun::getEndTime, query.getStartDate(), queryEndDate);
                    })
                    .orderByDesc(EquipRecordRun::getId);
            List<EquipRecordRunVo> equipRecordRunVos = EquipRecordRun.covert(this.list(qw), EquipRecordRunVo.class);
            if (CollectionUtil.isNotBlank(equipRecordRunVos)) {
                for (EquipRecordRunVo equipRecordRunVo : equipRecordRunVos) {
                    if (equipRecordRunVo.getEndTime() == null) {
                        equipRecordRunVo.setEndTime(queryEndDate);
                    }
                    if (equipRecordRunVo.getStartTime().before(queryEndDate)) {
                        if (equipRecordRunVo.getStartTime().before(query.getStartDate())) {
                            equipRecordRunVo.setStartTime(query.getStartDate());
                        }
                        r.add(equipRecordRunVo);
                    }
                }
            }
        }
        return r;
    }
}
