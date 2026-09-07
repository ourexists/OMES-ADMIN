/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.device.mapper.EquipRecordOnlineMapper;
import com.ourexists.omes.device.model.EquipRecordCountQuery;
import com.ourexists.omes.device.model.EquipRecordOnlineDto;
import com.ourexists.omes.device.model.EquipRecordOnlinePageQuery;
import com.ourexists.omes.device.model.EquipRecordOnlineVo;
import com.ourexists.omes.device.pojo.EquipRecordEventEndPatch;
import com.ourexists.omes.device.pojo.EquipRecordOnline;
import com.ourexists.omes.device.service.EquipRecordOnlineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class EquipRecordOnlineServiceImpl extends AbstractMyBatisPlusService<EquipRecordOnlineMapper, EquipRecordOnline>
        implements EquipRecordOnlineService {

    @Override
    public Page<EquipRecordOnline> selectByPage(EquipRecordOnlinePageQuery dto) {
        LambdaQueryWrapper<EquipRecordOnline> qw = new LambdaQueryWrapper<EquipRecordOnline>()
                .eq(StringUtils.hasText(dto.getSn()), EquipRecordOnline::getSn, dto.getSn())
                .eq(dto.getState() != null, EquipRecordOnline::getState, dto.getState())
                .and(dto.getStartDate() != null && dto.getEndDate() != null, wrapper -> {
                    wrapper
                            .between(EquipRecordOnline::getStartTime, dto.getStartDate(), dto.getEndDate())
                            .or()
                            .between(EquipRecordOnline::getEndTime, dto.getStartDate(), dto.getEndDate());
                })
                .orderByDesc(EquipRecordOnline::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        this.removeBatchByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(EquipRecordOnlineDto dto) {
        dto.setId(null);
        EquipRecordOnline current = EquipRecordOnline.wrap(dto);
        if (StringUtils.hasText(dto.getPrevEventId())) {
            EquipRecordOnline toClose = this.getOne(new LambdaQueryWrapper<EquipRecordOnline>()
                    .eq(EquipRecordOnline::getEventId, dto.getPrevEventId())
                    .eq(EquipRecordOnline::getSn, dto.getSn())
                    .last("limit 1"));
            if (toClose != null) {
                if (!toClose.getState().equals(current.getState())) {
                    toClose.setEndTime(current.getStartTime());
                    this.updateById(toClose);
                    this.save(current);
                }
                return;
            }
        }
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBatch(List<EquipRecordOnlineDto> ordered) {
        if (CollectionUtil.isBlank(ordered)) {
            return;
        }
        LinkedHashMap<String, EquipRecordEventEndPatch> closePatchBySnEvent = new LinkedHashMap<>();
        for (EquipRecordOnlineDto dto : ordered) {
            dto.setId(null);
            if (StringUtils.hasText(dto.getPrevEventId()) && StringUtils.hasText(dto.getSn())) {
                String key = dto.getSn() + "\0" + dto.getPrevEventId();
                closePatchBySnEvent.put(key, new EquipRecordEventEndPatch(dto.getSn(), dto.getPrevEventId(), dto.getStartTime()));
            }
        }
        if (!closePatchBySnEvent.isEmpty()) {
            baseMapper.batchUpdateEndTimeByEventId(new ArrayList<>(closePatchBySnEvent.values()));
        }
        this.saveBatch(EquipRecordOnline.wrap(ordered));
    }

    @Override
    public List<EquipRecordOnlineVo> countMerging(EquipRealtime equipRealtime, EquipRecordCountQuery query) {
        List<EquipRecordOnlineVo> segments = new ArrayList<>();
        query.capEndDateToNow();
        Date queryWindowEnd = query.getEndDate();
        if (equipRealtime != null && equipRealtime.getOnlineChangeTime() != null
                && !equipRealtime.getOnlineChangeTime().after(query.getStartDate())) {
            EquipRecordOnlineVo e = new EquipRecordOnlineVo();
            e.setSn(query.getSn());
            e.setStartTime(query.getStartDate());
            e.setEndTime(queryWindowEnd);
            e.setState(equipRealtime.getOnlineState());
            e.setTenantId(equipRealtime.getTenantId());
            segments.add(e);
        } else {
            Date queryEndDate;
            if (equipRealtime != null && equipRealtime.getOnlineChangeTime() != null &&
                    equipRealtime.getOnlineChangeTime().before(queryWindowEnd)) {
                EquipRecordOnlineVo e = new EquipRecordOnlineVo();
                e.setSn(query.getSn());
                e.setStartTime(equipRealtime.getOnlineChangeTime());
                e.setEndTime(queryWindowEnd);
                e.setState(equipRealtime.getOnlineState());
                e.setTenantId(equipRealtime.getTenantId());
                segments.add(e);
                queryEndDate = equipRealtime.getOnlineChangeTime();
            } else {
                queryEndDate = queryWindowEnd;
            }
            // 先按查询时间范围拉取重叠的在线区间
            LambdaQueryWrapper<EquipRecordOnline> qw = new LambdaQueryWrapper<EquipRecordOnline>()
                    .eq(EquipRecordOnline::getSn, query.getSn())
                    .and(wrapper -> {
                        wrapper
                                .between(EquipRecordOnline::getStartTime, query.getStartDate(), queryWindowEnd)
                                .or()
                                .between(EquipRecordOnline::getEndTime, query.getStartDate(), queryWindowEnd);
                    })
                    .orderByDesc(EquipRecordOnline::getId);
            List<EquipRecordOnlineVo> vos = EquipRecordOnline.covert(this.list(qw), EquipRecordOnlineVo.class);
            if (CollectionUtil.isNotBlank(vos)) {
                // 尾部已由实时态合成时，去掉库中与该段重复的区间（起点不早于 onlineChangeTime）
                if (queryEndDate.before(queryWindowEnd)) {
                    vos.removeIf(vo -> vo.getStartTime() != null && !vo.getStartTime().before(queryEndDate));
                }
                for (EquipRecordOnlineVo vo : vos) {
                    if (vo.getStartTime() != null && vo.getStartTime().before(queryWindowEnd)) {
                        segments.add(vo);
                    }
                }
            }
        }
        EquipRecordGanttSupport.normalizeOnlineForGantt(segments, query.getStartDate(), queryWindowEnd);
        return segments;
    }
}
