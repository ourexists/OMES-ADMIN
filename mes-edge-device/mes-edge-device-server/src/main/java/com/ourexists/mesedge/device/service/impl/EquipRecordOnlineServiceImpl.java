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
import com.ourexists.mesedge.device.mapper.EquipRecordOnlineMapper;
import com.ourexists.mesedge.device.model.EquipRecordCountQuery;
import com.ourexists.mesedge.device.model.EquipRecordOnlineDto;
import com.ourexists.mesedge.device.model.EquipRecordOnlinePageQuery;
import com.ourexists.mesedge.device.model.EquipRecordOnlineVo;
import com.ourexists.mesedge.device.pojo.EquipRecordOnline;
import com.ourexists.mesedge.device.service.EquipRecordOnlineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class EquipRecordOnlineServiceImpl extends AbstractMyBatisPlusService<EquipRecordOnlineMapper, EquipRecordOnline>
        implements EquipRecordOnlineService {

    @Autowired
    private EquipRealtimeManager realtimeManager;

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
    public List<EquipRecordOnlineVo> countMerging(EquipRecordCountQuery query) {
        List<EquipRecordOnlineVo> r = new ArrayList<>();
        EquipRealtime equipRealtime = realtimeManager.get(query.getSn());
        Date now = new Date();
        //限制最大查询时间不能大于当前时间
        if (query.getEndDate().after(now)) {
            query.setEndDate(now);
        }
        if (equipRealtime != null && equipRealtime.getOnlineChangeTime() != null
                && !equipRealtime.getOnlineChangeTime().after(query.getStartDate())) {
            EquipRecordOnlineVo e = new EquipRecordOnlineVo();
            e.setSn(query.getSn());
            e.setStartTime(query.getStartDate());
            e.setEndTime(query.getEndDate());
            e.setState(equipRealtime.getOnlineState());
            e.setTenantId(equipRealtime.getTenantId());
            r.add(e);
        } else {
            Date queryEndDate;
            if (equipRealtime != null && equipRealtime.getOnlineChangeTime() != null &&
                    equipRealtime.getOnlineChangeTime().before(query.getEndDate())) {
                EquipRecordOnlineVo e = new EquipRecordOnlineVo();
                e.setSn(query.getSn());
                e.setStartTime(equipRealtime.getOnlineChangeTime());
                e.setEndTime(query.getEndDate());
                e.setState(equipRealtime.getRunState());
                e.setTenantId(equipRealtime.getTenantId());
                r.add(e);
                queryEndDate = equipRealtime.getOnlineChangeTime();
            } else {
                queryEndDate = query.getEndDate();
            }
            LambdaQueryWrapper<EquipRecordOnline> qw = new LambdaQueryWrapper<EquipRecordOnline>()
                    .eq(EquipRecordOnline::getSn, query.getSn())
                    .and(wrapper -> {
                        wrapper
                                .between(EquipRecordOnline::getStartTime, query.getStartDate(), queryEndDate)
                                .or()
                                .between(EquipRecordOnline::getEndTime, query.getStartDate(), queryEndDate);
                    })
                    .orderByDesc(EquipRecordOnline::getId);
            List<EquipRecordOnlineVo> vos = EquipRecordOnline.covert(this.list(qw), EquipRecordOnlineVo.class);
            if (CollectionUtil.isNotBlank(vos)) {
                for (EquipRecordOnlineVo vo : vos) {
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
