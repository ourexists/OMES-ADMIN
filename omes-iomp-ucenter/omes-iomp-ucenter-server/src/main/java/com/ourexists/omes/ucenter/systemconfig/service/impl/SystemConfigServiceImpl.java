/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.ucenter.systemconfig.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.ucenter.systemconfig.SystemConfigDto;
import com.ourexists.omes.ucenter.systemconfig.SystemConfigKeys;
import com.ourexists.omes.ucenter.systemconfig.SystemConfigValue;
import com.ourexists.omes.ucenter.systemconfig.mapper.SystemConfigMapper;
import com.ourexists.omes.ucenter.systemconfig.pojo.SystemConfig;
import com.ourexists.omes.ucenter.systemconfig.service.SystemConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SystemConfigServiceImpl
        extends AbstractMyBatisPlusService<SystemConfigMapper, SystemConfig>
        implements SystemConfigService {

    @Override
    public SystemConfigDto getAppConfig() {
        SystemConfig entity = getByKey(SystemConfigKeys.APP);
        if (entity == null) {
            SystemConfigDto dto = new SystemConfigDto();
            dto.setConfigKey(SystemConfigKeys.APP);
            dto.setConfig(new SystemConfigValue());
            return dto;
        }
        return SystemConfig.toDto(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAppConfig(SystemConfigDto dto) {
        SystemConfigValue value = dto.getConfig() != null ? dto.getConfig() : new SystemConfigValue();
        if (value.getBaiduMapAk() != null) {
            value.setBaiduMapAk(value.getBaiduMapAk().trim());
        }

        SystemConfig existing = getByKey(SystemConfigKeys.APP);
        if (existing == null) {
            SystemConfig entity = new SystemConfig();
            entity.setId(IdWorker.getIdStr());
            entity.setConfigKey(SystemConfigKeys.APP);
            entity.setConfig(value);
            this.save(entity);
            return;
        }
        existing.setConfig(value);
        this.updateById(existing);
    }

    @Override
    public String getBaiduMapAk() {
        SystemConfigDto dto = getAppConfig();
        if (dto.getConfig() == null) {
            return "";
        }
        String ak = dto.getConfig().getBaiduMapAk();
        return StringUtils.hasText(ak) ? ak.trim() : "";
    }

    private SystemConfig getByKey(String configKey) {
        return this.getOne(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getConfigKey, configKey)
                        .last("LIMIT 1")
        );
    }
}
