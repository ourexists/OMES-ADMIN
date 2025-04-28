/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.sync.enums.SyncStatusEnum;
import com.ourexists.mesedge.sync.model.query.SyncPageQuery;
import com.ourexists.mesedge.sync.pojo.Sync;

import java.util.Date;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface SyncService extends IMyBatisPlusService<Sync> {


    void updateStatus(String id, SyncStatusEnum syncStatusEnum);

    Page<Sync> selectByPage(SyncPageQuery dto);

    /**
     * 查询最后一次分片同步数据
     *
     * @param syncName
     * @return
     */
    Sync selectLastSync(String syncName);

    void clearHistory(Date date);
}
