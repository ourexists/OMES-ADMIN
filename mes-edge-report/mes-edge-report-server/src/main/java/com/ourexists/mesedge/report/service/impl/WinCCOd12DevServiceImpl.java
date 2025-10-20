/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.report.mapper.WinCCOd12DevMapper;
import com.ourexists.mesedge.report.model.WinCCOd12Dev;
import com.ourexists.mesedge.report.model.WinCCOd12PageQuery;
import com.ourexists.mesedge.report.service.WinCCOd12DevService;
import org.springframework.stereotype.Service;

@Service
public class WinCCOd12DevServiceImpl extends AbstractMyBatisPlusService<WinCCOd12DevMapper, WinCCOd12Dev>
        implements WinCCOd12DevService {

    @Override
    public Page<WinCCOd12Dev> selectByPage(WinCCOd12PageQuery dto) {
        LambdaQueryWrapper<WinCCOd12Dev> qw = new LambdaQueryWrapper<WinCCOd12Dev>()
                .between(dto.getStartDate() != null && dto.getEndDate() != null, WinCCOd12Dev::getExecTime, dto.getStartDate(), dto.getEndDate())
                .orderByDesc(WinCCOd12Dev::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

}
