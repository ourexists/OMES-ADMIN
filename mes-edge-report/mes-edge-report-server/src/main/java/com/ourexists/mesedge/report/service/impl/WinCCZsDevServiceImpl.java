/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.report.mapper.WinCCZsDevMapper;
import com.ourexists.mesedge.report.model.WinCCZsDev;
import com.ourexists.mesedge.report.model.WinCCZsPageQuery;
import com.ourexists.mesedge.report.service.WinCCZsDevService;
import org.springframework.stereotype.Service;

@Service
public class WinCCZsDevServiceImpl extends AbstractMyBatisPlusService<WinCCZsDevMapper,  WinCCZsDev>
        implements WinCCZsDevService {

    @Override
    public Page<WinCCZsDev> selectByPage(WinCCZsPageQuery dto) {
        LambdaQueryWrapper< WinCCZsDev> qw = new LambdaQueryWrapper< WinCCZsDev>()
                .between(dto.getStartDate() != null && dto.getEndDate() != null,  WinCCZsDev::getExecTime, dto.getStartDate(), dto.getEndDate())
                .orderByDesc( WinCCZsDev::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

}
