/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.sync.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.sync.model.query.DeviceCollectBindingPageQuery;
import com.ourexists.omes.sync.pojo.DeviceCollectBinding;

import java.util.List;

public interface DeviceCollectBindingService extends IMyBatisPlusService<DeviceCollectBinding> {

    Page<DeviceCollectBinding> selectByPage(DeviceCollectBindingPageQuery query);

    List<DeviceCollectBinding> listByConnectIdAndSourceKeys(String connectId, List<String> sourceKeys);
}
