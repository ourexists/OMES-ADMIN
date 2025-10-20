/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.report.mapper.WinCCOd11DevMapper;
import com.ourexists.mesedge.report.model.WinCCOd11Dev;
import com.ourexists.mesedge.report.model.WinCCOd11PageQuery;
import com.ourexists.mesedge.report.service.WinCCOd11DevService;
import org.springframework.stereotype.Service;

@Service
public class WinCCOd11DevServiceImpl extends AbstractMyBatisPlusService<WinCCOd11DevMapper, WinCCOd11Dev>
        implements WinCCOd11DevService {

    @Override
    public Page<WinCCOd11Dev> selectByPage(WinCCOd11PageQuery dto) {
        LambdaQueryWrapper<WinCCOd11Dev> qw = new LambdaQueryWrapper<WinCCOd11Dev>()
                .between(dto.getStartDate() != null && dto.getEndDate() != null, WinCCOd11Dev::getExecTime, dto.getStartDate(), dto.getEndDate())
                .orderByDesc(WinCCOd11Dev::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

}
