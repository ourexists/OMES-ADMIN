/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.sync.service;

import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.sync.enums.SyncTxEnum;
import com.ourexists.omes.sync.pojo.SyncResource;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface SyncResourceService extends IMyBatisPlusService<SyncResource> {

    SyncResource getLastFlow(String syncId);

    List<SyncResource> selectBySyncId(String syncId);

    List<SyncResource> selectBySyncId(List<String> syncIds);

    boolean existSync(SyncTxEnum syncTxEnum, String reqData);
}
