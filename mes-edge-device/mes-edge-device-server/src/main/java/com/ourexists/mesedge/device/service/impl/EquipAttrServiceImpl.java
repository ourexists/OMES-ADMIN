/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.device.core.EquipAttrRealtime;
import com.ourexists.mesedge.device.core.EquipRealtime;
import com.ourexists.mesedge.device.core.EquipRealtimeManager;
import com.ourexists.mesedge.device.mapper.EquipAttrMapper;
import com.ourexists.mesedge.device.model.EquipAttrBatchDto;
import com.ourexists.mesedge.device.model.EquipAttrDto;
import com.ourexists.mesedge.device.model.EquipAttrPageQuery;
import com.ourexists.mesedge.device.pojo.EquipAttr;
import com.ourexists.mesedge.device.service.EquipAttrService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class EquipAttrServiceImpl extends AbstractMyBatisPlusService<EquipAttrMapper, EquipAttr> implements EquipAttrService {

    @Autowired
    private EquipRealtimeManager equipRealtimeManager;

    @Override
    public Page<EquipAttr> selectByPage(EquipAttrPageQuery dto) {
        LambdaQueryWrapper<EquipAttr> qw = new LambdaQueryWrapper<EquipAttr>().eq(StringUtils.hasText(dto.getEquipId()), EquipAttr::getEquipId, dto.getEquipId()).orderByAsc(EquipAttr::getSort, EquipAttr::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    public void addOrUpdate(EquipAttrDto dto) {
        saveOrUpdate(EquipAttr.wrap(dto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        this.removeBatchByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertBatch(EquipAttrBatchDto dto) {
        this.remove(new LambdaUpdateWrapper<EquipAttr>().eq(EquipAttr::getEquipId, dto.getEquipId()));
        if (CollectionUtils.isEmpty(dto.getAttrDtos())) {
            return;
        }
        dto.getAttrDtos().forEach(ecAttr -> {
            ecAttr.setEquipId(dto.getEquipId());
        });
        this.saveBatch(EquipAttr.wrap(dto.getAttrDtos()));


        EquipRealtime equipRealtime = equipRealtimeManager.getById(UserContext.getTenant().getTenantId(), dto.getEquipId());
        if (equipRealtime != null) {
            List<EquipAttrRealtime> equipAttrRealtimes = new ArrayList<>();
            if (!CollectionUtils.isEmpty(dto.getAttrDtos())) {
                for (EquipAttrDto attrDto : dto.getAttrDtos()) {
                    EquipAttrRealtime equipAttrRealtime = new EquipAttrRealtime();
                    BeanUtils.copyProperties(attrDto, equipAttrRealtime);
                    equipAttrRealtimes.add(equipAttrRealtime);
                }
            }
            equipRealtime.setEquipAttrRealtimes(equipAttrRealtimes);
            equipRealtimeManager.addOrUpdate(UserContext.getTenant().getTenantId(), equipRealtime);
        }
    }

    @Override
    public List<EquipAttr> queryByEquip(List<String> equipIds) {
        return this.list(new LambdaUpdateWrapper<EquipAttr>().in(EquipAttr::getEquipId, equipIds).orderByAsc(EquipAttr::getSort, EquipAttr::getId));
    }

    @Override
    public List<EquipAttr> queryByEquip(String equipId) {
        return this.list(new LambdaUpdateWrapper<EquipAttr>().eq(EquipAttr::getEquipId, equipId).orderByAsc(EquipAttr::getSort, EquipAttr::getId));
    }
}
