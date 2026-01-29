/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.device.core.equip.cache.EquipRealtime;
import com.ourexists.mesedge.device.core.equip.cache.EquipRealtimeManager;
import com.ourexists.mesedge.device.mapper.EquipRecordAlarmMapper;
import com.ourexists.mesedge.device.model.EquipRecordAlarmDto;
import com.ourexists.mesedge.device.model.EquipRecordAlarmPageQuery;
import com.ourexists.mesedge.device.model.EquipRecordAlarmVo;
import com.ourexists.mesedge.device.model.EquipRecordCountQuery;
import com.ourexists.mesedge.device.pojo.EquipRecordAlarm;
import com.ourexists.mesedge.device.service.EquipRecordAlarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class EquipRecordAlarmServiceImpl extends AbstractMyBatisPlusService<EquipRecordAlarmMapper, EquipRecordAlarm>
        implements EquipRecordAlarmService {

    @Autowired
    private EquipRealtimeManager realtimeManager;

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

    @Override
    public List<EquipRecordAlarmVo> countMerging(EquipRecordCountQuery query) {
        List<EquipRecordAlarmVo> r = new ArrayList<>();
        EquipRealtime equipRealtime = realtimeManager.get(query.getSn());
        Date now = new Date();
        //限制最大查询时间不能大于当前时间
        if (query.getEndDate().after(now)) {
            query.setEndDate(now);
        }
        if (equipRealtime != null && equipRealtime.getAlarmChangeTime() != null
                && !equipRealtime.getAlarmChangeTime().after(query.getStartDate())) {
            EquipRecordAlarmVo e = new EquipRecordAlarmVo();
            e.setSn(query.getSn());
            e.setStartTime(query.getStartDate());
            e.setEndTime(query.getEndDate());
            e.setState(equipRealtime.getAlarmState());
            e.setTenantId(equipRealtime.getTenantId());
            r.add(e);
        } else {
            Date queryEndDate;
            if (equipRealtime != null && equipRealtime.getAlarmChangeTime() != null &&
                    equipRealtime.getAlarmChangeTime().before(query.getEndDate())) {
                EquipRecordAlarmVo e = new EquipRecordAlarmVo();
                e.setSn(query.getSn());
                e.setStartTime(equipRealtime.getAlarmChangeTime());
                e.setEndTime(query.getEndDate());
                e.setState(equipRealtime.getAlarmState());
                e.setTenantId(equipRealtime.getTenantId());
                r.add(e);
                queryEndDate = equipRealtime.getAlarmChangeTime();
            } else {
                queryEndDate = query.getEndDate();
            }
            LambdaQueryWrapper<EquipRecordAlarm> qw = new LambdaQueryWrapper<EquipRecordAlarm>()
                    .eq(EquipRecordAlarm::getSn, query.getSn())
                    .and(wrapper -> {
                        wrapper
                                .between(EquipRecordAlarm::getStartTime, query.getStartDate(), queryEndDate)
                                .or()
                                .between(EquipRecordAlarm::getEndTime, query.getStartDate(), queryEndDate);
                    })
                    .orderByDesc(EquipRecordAlarm::getId);
            List<EquipRecordAlarmVo> vos = EquipRecordAlarm.covert(this.list(qw), EquipRecordAlarmVo.class);
            if (CollectionUtil.isNotBlank(vos)) {
                for (EquipRecordAlarmVo vo : vos) {
                    if (vo.getEndTime() == null) {
                        vo.setEndTime(queryEndDate);
                    }
                    if (vo.getStartTime().before(queryEndDate)) {
                        if (vo.getStartTime().before(query.getStartDate())) {
                            vo.setStartTime(query.getStartDate());
                        }
                        r.add(vo);
                    }
                }
            }
        }
        return r;
    }
}
