/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.sync.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.sync.enums.ProtocolEnum;
import com.ourexists.omes.sync.mapper.ConnectMapper;
import com.ourexists.omes.sync.model.query.ConnectPageQuery;
import com.ourexists.omes.sync.pojo.Connect;
import com.ourexists.omes.sync.service.ConnectService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    @Override
    public Page<Connect> selectByPage(ConnectPageQuery query) {
        LambdaQueryWrapper<Connect> wrapper = new LambdaQueryWrapper<Connect>()
                .eq(StringUtils.hasText(query.getProtocol()), Connect::getProtocol, query.getProtocol())
                .like(StringUtils.hasText(query.getServerName()), Connect::getServerName, query.getServerName())
                .like(StringUtils.hasText(query.getHost()), Connect::getHost, query.getHost())
                .orderByAsc(Connect::getId);
        return this.page(new Page<>(query.getPage(), query.getPageSize()), wrapper);
    }
}
