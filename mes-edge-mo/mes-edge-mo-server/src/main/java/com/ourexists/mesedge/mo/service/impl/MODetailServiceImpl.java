/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.mo.mapper.MODetailMapper;
import com.ourexists.mesedge.mo.pojo.MODetail;
import com.ourexists.mesedge.mo.model.query.MODetailPageQuery;
import com.ourexists.mesedge.mo.service.MODetailService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MODetailServiceImpl extends AbstractMyBatisPlusService<MODetailMapper, MODetail> implements MODetailService {
    @Override
    public Page<MODetail> selectByPage(MODetailPageQuery dto) {
        LambdaQueryWrapper<MODetail> qw = new LambdaQueryWrapper<MODetail>()
                .eq(StringUtils.isNotEmpty(dto.getMcode()), MODetail::getMcode, dto.getMcode())
                .orderByDesc(MODetail::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    public List<MODetail> selectByMcode(String mcode) {
        return list(new LambdaQueryWrapper<MODetail>().eq(MODetail::getMcode, mcode).orderByAsc(MODetail::getId));
    }
}
