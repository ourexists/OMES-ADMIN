/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.sync.enums.ProtocolEnum;
import com.ourexists.mesedge.sync.mapper.ConnectMapper;
import com.ourexists.mesedge.sync.pojo.Connect;
import com.ourexists.mesedge.sync.service.ConnectService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConnectServiceImpl extends AbstractMyBatisPlusService<ConnectMapper, Connect> implements ConnectService {

    @Override
    public List<Connect> getConnectByProtocol(ProtocolEnum protocol) {
        return this.list(new LambdaQueryWrapper<Connect>().eq(Connect::getProtocol, protocol.name()).orderByAsc(Connect::getId));
    }

    @Override
    public Connect getConnect(String serverName) {
        return this.getOne(new LambdaQueryWrapper<Connect>().eq(Connect::getServerName, serverName).last("limit 1"));
    }
}
