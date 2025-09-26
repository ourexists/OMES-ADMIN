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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class LmRecordServiceImpl extends AbstractMyBatisPlusService<LmRecordMapper, LmRecord>
        implements LmRecordService {

    @Override
    public Page<LmRecord> selectByPage(LmRecordPageQuery dto) {
        LambdaQueryWrapper<LmRecord> qw = new LambdaQueryWrapper<LmRecord>()
                .eq(dto.getFzId() != null, LmRecord::getFzId, dto.getFzId())
                .eq(StringUtils.isNotEmpty(dto.getMatName()), LmRecord::getLm, dto.getMatName())
                .eq(StringUtils.isNotEmpty(dto.getBh()), LmRecord::getBh, dto.getBh())
                .eq(StringUtils.isNotEmpty(dto.getLine()), LmRecord::getLine, dto.getLine())
                .orderByDesc(LmRecord::getNo);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }
}
