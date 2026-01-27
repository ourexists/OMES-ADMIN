/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.device.core.*;
import com.ourexists.mesedge.device.mapper.EquipConfigMapper;
import com.ourexists.mesedge.device.model.EquipAttrPageQuery;
import com.ourexists.mesedge.device.model.EquipConfigDto;
import com.ourexists.mesedge.device.pojo.EquipConfig;
import com.ourexists.mesedge.device.service.EquipConfigService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class EquipConfigServiceImpl extends AbstractMyBatisPlusService<EquipConfigMapper, EquipConfig>
        implements EquipConfigService {

    @Autowired
    private EquipRealtimeManager equipRealtimeManager;

    @Override
    public Page<EquipConfig> selectByPage(EquipAttrPageQuery dto) {
        LambdaQueryWrapper<EquipConfig> qw = new LambdaQueryWrapper<EquipConfig>().eq(StringUtils.hasText(dto.getEquipId()), EquipConfig::getEquipId, dto.getEquipId()).orderByAsc(EquipConfig::getEquipId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    public void addOrUpdate(EquipConfigDto dto) {
        saveOrUpdate(EquipConfig.wrap(dto));
        EquipRealtime equipRealtime = equipRealtimeManager.getById(UserContext.getTenant().getTenantId(), dto.getEquipId());
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
            equipRealtime.setEquipRealtimeConfig(equipRealtimeConfig);
            equipRealtime.setEquipAttrRealtimes(equipRealtimeConfig.getAttrs());
            equipRealtimeManager.addOrUpdate(UserContext.getTenant().getTenantId(), equipRealtime);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        this.removeBatchByIds(ids);
    }

    @Override
    public List<EquipConfig> queryByEquip(List<String> equipIds) {
        return this.list(new LambdaUpdateWrapper<EquipConfig>().in(EquipConfig::getEquipId, equipIds));
    }

    @Override
    public EquipConfig queryByEquip(String equipId) {
        return this.getOne(new LambdaUpdateWrapper<EquipConfig>().eq(EquipConfig::getEquipId, equipId));
    }
}
