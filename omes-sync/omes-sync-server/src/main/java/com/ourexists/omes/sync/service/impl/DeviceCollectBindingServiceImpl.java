/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.sync.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.sync.mapper.DeviceCollectBindingMapper;
import com.ourexists.omes.sync.model.query.DeviceCollectBindingPageQuery;
import com.ourexists.omes.sync.pojo.DeviceCollectBinding;
import com.ourexists.omes.sync.service.DeviceCollectBindingService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
public class DeviceCollectBindingServiceImpl extends AbstractMyBatisPlusService<DeviceCollectBindingMapper, DeviceCollectBinding> implements DeviceCollectBindingService {

    @Override
    public Page<DeviceCollectBinding> selectByPage(DeviceCollectBindingPageQuery query) {
        LambdaQueryWrapper<DeviceCollectBinding> w = new LambdaQueryWrapper<DeviceCollectBinding>()
                .eq(StringUtils.hasText(query.getConnectId()), DeviceCollectBinding::getConnectId, query.getConnectId())
                .like(StringUtils.hasText(query.getEquipSn()), DeviceCollectBinding::getEquipSn, query.getEquipSn())
                .orderByDesc(DeviceCollectBinding::getCreatedTime);
        return this.page(new Page<>(query.getPage(), query.getPageSize()), w);
    }

    @Override
    public List<DeviceCollectBinding> listByConnectIdAndSourceKeys(String connectId, List<String> sourceKeys) {
        if (!StringUtils.hasText(connectId) || sourceKeys == null || sourceKeys.isEmpty()) return Collections.emptyList();
        return this.list(new LambdaQueryWrapper<DeviceCollectBinding>()
                .eq(DeviceCollectBinding::getConnectId, connectId)
                .in(DeviceCollectBinding::getSourceKey, sourceKeys));
    }
}
