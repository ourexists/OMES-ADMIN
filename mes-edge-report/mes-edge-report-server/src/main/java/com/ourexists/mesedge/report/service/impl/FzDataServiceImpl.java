/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.report.mapper.FzDataMapper;
import com.ourexists.mesedge.report.model.FzData;
import com.ourexists.mesedge.report.model.FzDataPageQuery;
import com.ourexists.mesedge.report.service.FzDataService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FzDataServiceImpl extends AbstractMyBatisPlusService<FzDataMapper, FzData>
        implements FzDataService {

    @Override
    public Page<FzData> selectByPage(FzDataPageQuery dto) {
        LambdaQueryWrapper<FzData> qw = new LambdaQueryWrapper<FzData>()
                .between(dto.getStartDate() != null && dto.getEndDate() != null, FzData::getRq, dto.getStartDate(), dto.getEndDate())
                .like(StringUtils.isNotEmpty(dto.getPfName()), FzData::getPf, dto.getPfName())
                .eq(StringUtils.isNotEmpty(dto.getBh()), FzData::getBh, dto.getBh())
                .eq(StringUtils.isNotEmpty(dto.getLine()), FzData::getLine, dto.getLine())
                .inSql(StringUtils.isNotEmpty(dto.getMatName()), FzData::getNo, "select FZ_ID from lm_record where LM like '%" + dto.getMatName() + "%'")
                .orderByDesc(FzData::getNo);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    public List<String> allPFName() {
        return this.baseMapper.allPFNames();
    }
}
