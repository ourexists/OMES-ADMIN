/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.device.core.equip.protocol.ProtocolConnect;
import com.ourexists.omes.device.core.equip.protocol.ProtocolExecutor;
import com.ourexists.omes.device.mapper.GatewayMapper;
import com.ourexists.omes.device.model.GatewayPageQuery;
import com.ourexists.omes.device.pojo.Gateway;
import com.ourexists.omes.device.service.GatewayService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class GatewayServiceImpl extends AbstractMyBatisPlusService<GatewayMapper, Gateway> implements GatewayService {

    @Autowired
    private ProtocolExecutor protocolExecutor;

    @Override
    public List<Gateway> getConnectByProtocol(String protocol) {
        return this.list(new LambdaQueryWrapper<Gateway>().eq(Gateway::getProtocol, protocol).orderByAsc(Gateway::getId));
    }

    @Override
    public Gateway getConnect(String serverName) {
        return this.getOne(new LambdaQueryWrapper<Gateway>().eq(Gateway::getServerName, serverName).last("limit 1"));
    }

    @Override
    public Page<Gateway> selectByPage(GatewayPageQuery query) {
        LambdaQueryWrapper<Gateway> wrapper = new LambdaQueryWrapper<Gateway>()
                .eq(StringUtils.hasText(query.getProtocol()), Gateway::getProtocol, query.getProtocol())
                .eq(query.getEnabled() != null, Gateway::getEnabled, query.getEnabled())
                .like(StringUtils.hasText(query.getServerName()), Gateway::getServerName, query.getServerName())
                .orderByAsc(Gateway::getId);
        return this.page(new Page<>(query.getPage(), query.getPageSize()), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void start(String id) {
        Gateway gateway = this.getById(id);
        if (gateway == null) {
            return;
        }
        this.update(new LambdaUpdateWrapper<Gateway>()
                .set(Gateway::getEnabled, true)
                .eq(Gateway::getId, id)
        );
        ProtocolConnect connect = new ProtocolConnect();
        BeanUtils.copyProperties(gateway, connect);
        protocolExecutor.start(connect);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stop(String id) {
        Gateway gateway = this.getById(id);
        if (gateway == null) {
            return;
        }
        this.update(new LambdaUpdateWrapper<Gateway>()
                .set(Gateway::getEnabled, false)
                .eq(Gateway::getId, id)
        );
        protocolExecutor.stop(gateway.getProtocol(), id);
    }
}
