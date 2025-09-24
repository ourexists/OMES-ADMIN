/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.report.mapper.LmRecordMapper;
import com.ourexists.mesedge.report.model.LmRecord;
import com.ourexists.mesedge.report.model.LmRecordPageQuery;
import com.ourexists.mesedge.report.service.LmRecordService;
import org.springframework.stereotype.Service;

@Service
public class LmRecordServiceImpl extends AbstractMyBatisPlusService<LmRecordMapper, LmRecord>
        implements LmRecordService {

    @Override
    public Page<LmRecord> selectByPage(LmRecordPageQuery dto) {
        LambdaQueryWrapper<LmRecord> qw = new LambdaQueryWrapper<LmRecord>()
                .eq(dto.getFzId() != null, LmRecord::getFzId, dto.getFzId())
                .orderByDesc(LmRecord::getNo);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }
}
