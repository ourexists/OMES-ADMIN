/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.mo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.mo.mapper.MoAdjustLogMapper;
import com.ourexists.omes.mo.pojo.MoAdjustLog;
import com.ourexists.omes.mo.service.MoAdjustLogService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MoAdjustLogServiceImpl extends AbstractMyBatisPlusService<MoAdjustLogMapper, MoAdjustLog>
        implements MoAdjustLogService {

    @Override
    public MoAdjustLog selectByRequestId(String requestId) {
        return this.getOne(new LambdaQueryWrapper<MoAdjustLog>()
                .eq(MoAdjustLog::getRequestId, requestId)
                .last("limit 1"));
    }

    @Override
    public List<MoAdjustLog> selectByMoCode(String moCode) {
        return this.list(new LambdaQueryWrapper<MoAdjustLog>()
                .eq(MoAdjustLog::getMoCode, moCode)
                .orderByDesc(MoAdjustLog::getCreateTime)
                .orderByDesc(MoAdjustLog::getId));
    }
}
