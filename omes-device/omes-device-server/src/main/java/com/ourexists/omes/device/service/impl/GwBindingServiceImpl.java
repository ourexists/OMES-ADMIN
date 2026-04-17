/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.device.core.equip.cache.*;
import com.ourexists.omes.device.mapper.GwBindingMapper;
import com.ourexists.omes.device.model.EquipAttrPageQuery;
import com.ourexists.omes.device.model.GwBindingDto;
import com.ourexists.omes.device.pojo.GwBinding;
import com.ourexists.omes.device.service.GwBindingService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class GwBindingServiceImpl extends AbstractMyBatisPlusService<GwBindingMapper, GwBinding>
        implements GwBindingService {

    @Autowired
    private EquipRealtimeManager equipRealtimeManager;

    @Override
    public Page<GwBinding> selectByPage(EquipAttrPageQuery dto) {
        LambdaQueryWrapper<GwBinding> qw = new LambdaQueryWrapper<GwBinding>().eq(StringUtils.hasText(dto.getEquipId()), GwBinding::getEquipId, dto.getEquipId()).orderByAsc(GwBinding::getEquipId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    public void addOrUpdate(GwBindingDto dto) {
        dto.getConfig().setGwId(dto.getGwId());
        this.baseMapper.upsertByEquipId(GwBinding.wrap(dto));
        EquipRealtime equipRealtime = equipRealtimeManager.getById(dto.getEquipId());
        if (equipRealtime != null) {
            EquipRealtimeConfig equipRealtimeConfig = new EquipRealtimeConfig();
            BeanUtils.copyProperties(dto.getConfig(), equipRealtimeConfig);
            if (!CollectionUtils.isEmpty(dto.getConfig().getAttrs())) {
                List<EquipAttrRealtime> attrs = new ArrayList<>();
                dto.getConfig().getAttrs().forEach(attr -> {
                    EquipAttrRealtime equipAttrRealtime = new EquipAttrRealtime();
                    BeanUtils.copyProperties(attr, equipAttrRealtime);
                    attrs.add(equipAttrRealtime);
                });
                equipRealtimeConfig.setAttrs(attrs);
            }
            if (!CollectionUtils.isEmpty(dto.getConfig().getAlarms())) {
                List<EquipAlarmRealtime> alarms = new ArrayList<>();
                dto.getConfig().getAlarms().forEach(attr -> {
                    EquipAlarmRealtime equipAlarmRealtime = new EquipAlarmRealtime();
                    BeanUtils.copyProperties(attr, equipAlarmRealtime);
                    alarms.add(equipAlarmRealtime);
                });
                equipRealtimeConfig.setAlarms(alarms);
            }
            if (!CollectionUtils.isEmpty(dto.getConfig().getControls())) {
                List<EquipControlRealtime> controls = new ArrayList<>();
                dto.getConfig().getControls().forEach(ctrl -> {
                    EquipControlRealtime equipControlRealtime = new EquipControlRealtime();
                    BeanUtils.copyProperties(ctrl, equipControlRealtime);
                    controls.add(equipControlRealtime);
                });
                equipRealtimeConfig.setControls(controls);
            }
            equipRealtime.setEquipRealtimeConfig(equipRealtimeConfig);
            equipRealtime.setEquipAttrRealtimes(equipRealtimeConfig.getAttrs());
            equipRealtime.setEquipControlRealtimes(equipRealtimeConfig.getControls());
            equipRealtimeManager.addOrUpdate(equipRealtime);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        this.removeBatchByIds(ids);
    }

    @Override
    public List<GwBinding> queryByEquip(List<String> equipIds) {
        return this.list(new LambdaUpdateWrapper<GwBinding>().in(GwBinding::getEquipId, equipIds));
    }

    @Override
    public GwBinding queryByEquip(String equipId) {
        return this.getOne(new LambdaUpdateWrapper<GwBinding>().eq(GwBinding::getEquipId, equipId));
    }
}
