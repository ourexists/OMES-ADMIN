/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.sync.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.sync.enums.ProtocolEnum;
import com.ourexists.omes.sync.model.query.ConnectPageQuery;
import com.ourexists.omes.sync.pojo.Connect;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface ConnectService extends IMyBatisPlusService<Connect> {

    List<Connect> getConnectByProtocol(ProtocolEnum protocol);

    Connect getConnect(String serverName);

    Page<Connect> selectByPage(ConnectPageQuery query);
}
