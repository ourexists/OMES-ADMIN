/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.device.mapper.DeviceMapper;
import com.ourexists.omes.device.pojo.Device;
import com.ourexists.omes.device.service.DeviceService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceServiceImpl extends AbstractMyBatisPlusService<DeviceMapper, Device>
        implements DeviceService {

    @Override
    public Boolean isUseMat(List<String> matCodes) {
        return this.count(new LambdaQueryWrapper<Device>().in(Device::getMatCode, matCodes)) > 0;
    }
}
