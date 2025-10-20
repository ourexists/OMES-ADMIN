/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.report.mapper.WinCCWpsDevMapper;
import com.ourexists.mesedge.report.model.WinCCWpsDev;
import com.ourexists.mesedge.report.model.WinCCWpsPageQuery;
import com.ourexists.mesedge.report.service.WinCCWpsDevService;
import org.springframework.stereotype.Service;

@Service
public class WinCCWpsDevServiceImpl extends AbstractMyBatisPlusService<WinCCWpsDevMapper, WinCCWpsDev>
        implements WinCCWpsDevService {

    @Override
    public Page<WinCCWpsDev> selectByPage(WinCCWpsPageQuery dto) {
        LambdaQueryWrapper<WinCCWpsDev> qw = new LambdaQueryWrapper<WinCCWpsDev>()
                .between(dto.getStartDate() != null && dto.getEndDate() != null, WinCCWpsDev::getExecTime, dto.getStartDate(), dto.getEndDate())
                .orderByDesc(WinCCWpsDev::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

}
