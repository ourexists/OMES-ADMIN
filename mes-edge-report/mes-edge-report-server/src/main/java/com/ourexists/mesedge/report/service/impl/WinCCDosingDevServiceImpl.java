/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.report.mapper.WinCCDosingDevMapper;
import com.ourexists.mesedge.report.model.WinCCDosingDev;
import com.ourexists.mesedge.report.model.WinCCDosingPageQuery;
import com.ourexists.mesedge.report.service.WinCCDosingDevService;
import org.springframework.stereotype.Service;

@Service
public class WinCCDosingDevServiceImpl extends AbstractMyBatisPlusService<WinCCDosingDevMapper, WinCCDosingDev>
        implements WinCCDosingDevService {

    @Override
    public Page<WinCCDosingDev> selectByPage(WinCCDosingPageQuery dto) {
        LambdaQueryWrapper<WinCCDosingDev> qw = new LambdaQueryWrapper<WinCCDosingDev>()
                .between(dto.getStartDate() != null && dto.getEndDate() != null, WinCCDosingDev::getExecTime, dto.getStartDate(), dto.getEndDate())
                .orderByDesc(WinCCDosingDev::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

}
