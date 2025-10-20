/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.report.mapper.WinCCMagDevMapper;
import com.ourexists.mesedge.report.model.WinCCMagDev;
import com.ourexists.mesedge.report.model.WinCCMagPageQuery;
import com.ourexists.mesedge.report.service.WinCCMagDevService;
import org.springframework.stereotype.Service;

@Service
public class WinCCMagDevServiceImpl extends AbstractMyBatisPlusService<WinCCMagDevMapper, WinCCMagDev>
        implements WinCCMagDevService {

    @Override
    public Page<WinCCMagDev> selectByPage(WinCCMagPageQuery dto) {
        LambdaQueryWrapper<WinCCMagDev> qw = new LambdaQueryWrapper<WinCCMagDev>()
                .between(dto.getStartDate() != null && dto.getEndDate() != null, WinCCMagDev::getExecTime, dto.getStartDate(), dto.getEndDate())
                .orderByDesc(WinCCMagDev::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

}
