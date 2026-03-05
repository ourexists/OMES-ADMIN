/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.device.enums.ProtocolEnum;
import com.ourexists.omes.device.model.GatewayPageQuery;
import com.ourexists.omes.device.pojo.Gateway;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface GatewayService extends IMyBatisPlusService<Gateway> {

    List<Gateway> getConnectByProtocol(ProtocolEnum protocol);

    Gateway getConnect(String serverName);

    Page<Gateway> selectByPage(GatewayPageQuery query);

    void start(String id);

    void stop(String id);
}
