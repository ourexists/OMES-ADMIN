/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service.impl;

import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.device.mapper.DeviceMapper;
import com.ourexists.mesedge.device.pojo.Device;
import com.ourexists.mesedge.device.service.DeviceService;
import org.springframework.stereotype.Service;

@Service
public class DeviceServiceImpl extends AbstractMyBatisPlusService<DeviceMapper, Device>
        implements DeviceService {
}
