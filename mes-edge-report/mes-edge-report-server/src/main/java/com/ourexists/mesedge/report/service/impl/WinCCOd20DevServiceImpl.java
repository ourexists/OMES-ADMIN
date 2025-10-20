/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.report.mapper.WinCCOd20DevMapper;
import com.ourexists.mesedge.report.model.WinCCOd20Dev;
import com.ourexists.mesedge.report.model.WinCCOd20PageQuery;
import com.ourexists.mesedge.report.service.WinCCOd20DevService;
import org.springframework.stereotype.Service;

@Service
public class WinCCOd20DevServiceImpl extends AbstractMyBatisPlusService<WinCCOd20DevMapper, WinCCOd20Dev>
        implements WinCCOd20DevService {

    @Override
    public Page<WinCCOd20Dev> selectByPage(WinCCOd20PageQuery dto) {
        LambdaQueryWrapper<WinCCOd20Dev> qw = new LambdaQueryWrapper<WinCCOd20Dev>()
                .between(dto.getStartDate() != null && dto.getEndDate() != null, WinCCOd20Dev::getExecTime, dto.getStartDate(), dto.getEndDate())
                .orderByDesc(WinCCOd20Dev::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

}
