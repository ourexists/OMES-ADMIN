/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.mps.model.ChangePriorityDto;
import com.ourexists.mesedge.mps.model.MPSDto;
import com.ourexists.mesedge.mps.model.MPSQueueOperateDto;
import com.ourexists.mesedge.mps.enums.MPSStatusEnum;
import com.ourexists.mesedge.mps.pojo.MPS;
import com.ourexists.mesedge.mps.model.query.MPSPageQuery;

import java.util.Collection;
import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface MPSService extends IMyBatisPlusService<MPS> {

    Page<MPS> selectByPage(MPSPageQuery dto);

    <T extends MPSDto> void addBatch(List<T> dtos);

    void changePriority(ChangePriorityDto dto);

    void joinQueueBatch(List<String> ids);

    void joinQueue(MPSQueueOperateDto dto);

    void joinQueueBatchByMoCode(String moCode);

    void joinQueueBatchByMoCodes(List<String> moCodes);

    /**
     * 限制执行时间内的计划加入队列
     *
     * @param moCodes
     */
    void joinQueueBatchByMoCodesLimitEnable(List<String> moCodes);

    void removeQueue(MPSQueueOperateDto dto);

    void jumpQueue(MPSQueueOperateDto dto);

    /**
     * 查询当前可入对的生产任务
     *
     * @return
     */
    List<MPS> selectEnabledJoinQueMps();

    List<MPS> selectByMoCode(String moCode);

    List<MPS> selectByStatus(MPSStatusEnum status);

    List<MPS> selectByIds(Collection<String> ids);

    List<MPS> selectByIdsAndStatus(Collection<String> ids, MPSStatusEnum status);

    int getMaxBatch(String moCode);

    boolean updateStatus(String id, MPSStatusEnum mpsStatus);

    void adjustToJoinQue();
}
