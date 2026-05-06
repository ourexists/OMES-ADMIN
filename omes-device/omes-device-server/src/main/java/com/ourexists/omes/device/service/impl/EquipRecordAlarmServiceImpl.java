/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.device.core.equip.cache.EquipRealtimeManager;
import com.ourexists.omes.device.mapper.EquipRecordAlarmMapper;
import com.ourexists.omes.device.model.EquipRecordAlarmDto;
import com.ourexists.omes.device.model.EquipRecordAlarmPageQuery;
import com.ourexists.omes.device.model.EquipRecordAlarmVo;
import com.ourexists.omes.device.model.EquipRecordCountQuery;
import com.ourexists.omes.device.pojo.EquipRecordAlarm;
import com.ourexists.omes.device.pojo.EquipRecordEventEndPatch;
import com.ourexists.omes.device.service.EquipRecordAlarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class EquipRecordAlarmServiceImpl extends AbstractMyBatisPlusService<EquipRecordAlarmMapper, EquipRecordAlarm> implements EquipRecordAlarmService {

    @Autowired
    private EquipRealtimeManager realtimeManager;

    @Override
    public Page<EquipRecordAlarm> selectByPage(EquipRecordAlarmPageQuery dto) {
        LambdaQueryWrapper<EquipRecordAlarm> qw = new LambdaQueryWrapper<EquipRecordAlarm>().eq(StringUtils.hasText(dto.getSn()), EquipRecordAlarm::getSn, dto.getSn()).eq(dto.getState() != null, EquipRecordAlarm::getState, dto.getState()).and(dto.getStartDate() != null && dto.getEndDate() != null, wrapper -> {
            wrapper.between(EquipRecordAlarm::getStartTime, dto.getStartDate(), dto.getEndDate()).or().between(EquipRecordAlarm::getEndTime, dto.getStartDate(), dto.getEndDate());
        }).orderByDesc(EquipRecordAlarm::getId);
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
        if (StringUtils.hasText(dto.getPrevEventId())) {
            EquipRecordAlarm toClose = this.getOne(new LambdaQueryWrapper<EquipRecordAlarm>().eq(EquipRecordAlarm::getEventId, dto.getPrevEventId()).eq(EquipRecordAlarm::getSn, dto.getSn()).last("limit 1"));
            if (toClose != null) {
                if (!toClose.getState().equals(current.getState())) {
                    toClose.setEndTime(current.getStartTime());
                    this.updateById(toClose);
                    this.save(current);
                }
                return;
            }
        }
        EquipRecordAlarm last = this.getOne(new LambdaQueryWrapper<EquipRecordAlarm>().eq(EquipRecordAlarm::getSn, dto.getSn()).orderByDesc(EquipRecordAlarm::getStartTime, EquipRecordAlarm::getId).last("limit 1"));
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
    @Transactional(rollbackFor = Exception.class)
    public void addBatch(List<EquipRecordAlarmDto> ordered) {
        if (CollectionUtil.isBlank(ordered)) {
            return;
        }
        LinkedHashMap<String, EquipRecordEventEndPatch> closePatchBySnEvent = new LinkedHashMap<>();
        for (EquipRecordAlarmDto dto : ordered) {
            dto.setId(null);
            if (StringUtils.hasText(dto.getPrevEventId()) && StringUtils.hasText(dto.getSn())) {
                String key = dto.getSn() + "\0" + dto.getPrevEventId();
                closePatchBySnEvent.put(key, new EquipRecordEventEndPatch(dto.getSn(), dto.getPrevEventId(), dto.getStartTime()));
            }
        }
        if (!closePatchBySnEvent.isEmpty()) {
            baseMapper.batchUpdateEndTimeByEventId(new ArrayList<>(closePatchBySnEvent.values()));
        }
        this.saveBatch(EquipRecordAlarm.wrap(ordered));
    }

    @Override
    public List<EquipRecordAlarmVo> countMerging(EquipRecordCountQuery query) {
        List<EquipRecordAlarmVo> segments = new ArrayList<>();
        EquipRealtime equipRealtime = realtimeManager.get(query.getSn());
        query.capEndDateToNow();
        Date queryWindowEnd = query.getEndDate();
        if (equipRealtime != null && equipRealtime.getAlarmChangeTime() != null && !equipRealtime.getAlarmChangeTime().after(query.getStartDate())) {
            EquipRecordAlarmVo e = new EquipRecordAlarmVo();
            e.setSn(query.getSn());
            e.setStartTime(query.getStartDate());
            e.setEndTime(queryWindowEnd);
            e.setState(equipRealtime.getAlarmState());
            e.setTenantId(equipRealtime.getTenantId());
            segments.add(e);
        } else {
            Date queryEndDate;
            if (equipRealtime != null && equipRealtime.getAlarmChangeTime() != null && equipRealtime.getAlarmChangeTime().before(queryWindowEnd)) {
                EquipRecordAlarmVo e = new EquipRecordAlarmVo();
                e.setSn(query.getSn());
                e.setStartTime(equipRealtime.getAlarmChangeTime());
                e.setEndTime(queryWindowEnd);
                e.setState(equipRealtime.getAlarmState());
                e.setTenantId(equipRealtime.getTenantId());
                segments.add(e);
                queryEndDate = equipRealtime.getAlarmChangeTime();
            } else {
                queryEndDate = queryWindowEnd;
            }
            LambdaQueryWrapper<EquipRecordAlarm> qw = new LambdaQueryWrapper<EquipRecordAlarm>()
                    .eq(EquipRecordAlarm::getSn, query.getSn())
                    .and(wrapper -> {
                        wrapper
                                .between(EquipRecordAlarm::getStartTime, query.getStartDate(), queryWindowEnd)
                                .or()
                                .between(EquipRecordAlarm::getEndTime, query.getStartDate(), queryWindowEnd);
                    })
                    .orderByDesc(EquipRecordAlarm::getId);
            List<EquipRecordAlarmVo> vos = EquipRecordAlarm.covert(this.list(qw), EquipRecordAlarmVo.class);
            if (CollectionUtil.isNotBlank(vos)) {
                if (queryEndDate.before(queryWindowEnd)) {
                    vos.removeIf(vo -> vo.getStartTime() != null && !vo.getStartTime().before(queryEndDate));
                }
                for (EquipRecordAlarmVo vo : vos) {
                    if (vo.getStartTime() != null && vo.getStartTime().before(queryWindowEnd)) {
                        segments.add(vo);
                    }
                }
            }
        }
        EquipRecordGanttSupport.normalizeAlarmForGantt(segments, query.getStartDate(), queryWindowEnd);
        return segments;
    }
}
