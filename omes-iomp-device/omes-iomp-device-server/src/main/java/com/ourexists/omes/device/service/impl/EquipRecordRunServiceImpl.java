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
import com.ourexists.omes.device.mapper.EquipRecordRunMapper;
import com.ourexists.omes.device.model.EquipRecordCountQuery;
import com.ourexists.omes.device.model.EquipRecordRunDto;
import com.ourexists.omes.device.model.EquipRecordRunPageQuery;
import com.ourexists.omes.device.model.EquipRecordRunVo;
import com.ourexists.omes.device.pojo.EquipRecordEventEndPatch;
import com.ourexists.omes.device.pojo.EquipRecordRun;
import com.ourexists.omes.device.service.EquipRecordRunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class EquipRecordRunServiceImpl extends AbstractMyBatisPlusService<EquipRecordRunMapper, EquipRecordRun>
        implements EquipRecordRunService {

    /** 与实时侧 EquipRealtime 一致：{@code -1}（及 null）表示未知，不落运行区间行。 */
    private static final int RUN_STATE_UNKNOWN = -1;

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
        boolean insertNewSegment = current.getState() != null && !Objects.equals(current.getState(), RUN_STATE_UNKNOWN);
        if (StringUtils.hasText(dto.getPrevEventId())) {
            EquipRecordRun toClose = this.getOne(new LambdaQueryWrapper<EquipRecordRun>()
                    .eq(EquipRecordRun::getEventId, dto.getPrevEventId())
                    .eq(EquipRecordRun::getSn, dto.getSn())
                    .last("limit 1"));
            if (toClose != null) {
                if (!toClose.getState().equals(current.getState())) {
                    toClose.setEndTime(current.getStartTime());
                    this.updateById(toClose);
                    if (insertNewSegment) {
                        this.save(current);
                    }
                }
                return;
            }
        }
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
                if (insertNewSegment) {
                    this.save(current);
                }
            }
        } else {
            if (insertNewSegment) {
                this.save(current);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBatch(List<EquipRecordRunDto> ordered) {
        if (CollectionUtil.isBlank(ordered)) {
            return;
        }
        LinkedHashMap<String, EquipRecordEventEndPatch> closePatchBySnEvent = new LinkedHashMap<>();
        for (EquipRecordRunDto dto : ordered) {
            dto.setId(null);
            if (StringUtils.hasText(dto.getPrevEventId()) && StringUtils.hasText(dto.getSn())) {
                String key = dto.getSn() + "\0" + dto.getPrevEventId();
                closePatchBySnEvent.put(key, new EquipRecordEventEndPatch(dto.getSn(), dto.getPrevEventId(), dto.getStartTime()));
            }
        }
        if (!closePatchBySnEvent.isEmpty()) {
            baseMapper.batchUpdateEndTimeByEventId(new ArrayList<>(closePatchBySnEvent.values()));
        }
        List<EquipRecordRunDto> toInsert = ordered.stream()
                .filter(d -> d.getState() != null && !Objects.equals(d.getState(), RUN_STATE_UNKNOWN))
                .collect(Collectors.toList());
        if (!toInsert.isEmpty()) {
            this.saveBatch(EquipRecordRun.wrap(toInsert));
        }
    }

    @Override
    public List<EquipRecordRunVo> countMerging(EquipRecordCountQuery query) {
        List<EquipRecordRunVo> segments = new ArrayList<>();
        EquipRealtime equipRealtime = realtimeManager.get(query.getSn());
        query.capEndDateToNow();
        Date queryWindowEnd = query.getEndDate();
        if (equipRealtime != null && equipRealtime.getRunChangeTime() != null
                && !equipRealtime.getRunChangeTime().after(query.getStartDate())) {
            EquipRecordRunVo e = new EquipRecordRunVo();
            e.setSn(query.getSn());
            e.setStartTime(query.getStartDate());
            e.setEndTime(queryWindowEnd);
            e.setState(equipRealtime.getRunState());
            e.setTenantId(equipRealtime.getTenantId());
            segments.add(e);
        } else {
            Date queryEndDate;
            if (equipRealtime != null && equipRealtime.getRunChangeTime() != null &&
                    equipRealtime.getRunChangeTime().before(queryWindowEnd)) {
                EquipRecordRunVo e = new EquipRecordRunVo();
                e.setSn(query.getSn());
                e.setStartTime(equipRealtime.getRunChangeTime());
                e.setEndTime(queryWindowEnd);
                e.setState(equipRealtime.getRunState());
                e.setTenantId(equipRealtime.getTenantId());
                segments.add(e);
                queryEndDate = equipRealtime.getRunChangeTime();
            } else {
                queryEndDate = queryWindowEnd;
            }
            LambdaQueryWrapper<EquipRecordRun> qw = new LambdaQueryWrapper<EquipRecordRun>()
                    .eq(EquipRecordRun::getSn, query.getSn())
                    .and(wrapper -> {
                        wrapper
                                .between(EquipRecordRun::getStartTime, query.getStartDate(), queryWindowEnd)
                                .or()
                                .between(EquipRecordRun::getEndTime, query.getStartDate(), queryWindowEnd);
                    })
                    .orderByDesc(EquipRecordRun::getId);
            List<EquipRecordRunVo> equipRecordRunVos = EquipRecordRun.covert(this.list(qw), EquipRecordRunVo.class);
            if (CollectionUtil.isNotBlank(equipRecordRunVos)) {
                if (queryEndDate.before(queryWindowEnd)) {
                    equipRecordRunVos.removeIf(vo -> vo.getStartTime() != null && !vo.getStartTime().before(queryEndDate));
                }
                for (EquipRecordRunVo equipRecordRunVo : equipRecordRunVos) {
                    if (equipRecordRunVo.getStartTime() != null && equipRecordRunVo.getStartTime().before(queryWindowEnd)) {
                        segments.add(equipRecordRunVo);
                    }
                }
            }
        }
        EquipRecordGanttSupport.normalizeRunForGantt(segments, query.getStartDate(), queryWindowEnd);
        return segments;
    }

    @Override
    public Long sumRunMinutesBySn(String sn) {
        if (sn == null || sn.isEmpty()) return 0L;
        Long v = baseMapper.sumRunMinutesBySn(sn);
        return v != null ? v : 0L;
    }

    @Override
    public Long countRunSegmentsBySn(String sn) {
        if (sn == null || sn.isEmpty()) return 0L;
        Long v = baseMapper.countRunSegmentsBySn(sn);
        return v != null ? v : 0L;
    }
}
