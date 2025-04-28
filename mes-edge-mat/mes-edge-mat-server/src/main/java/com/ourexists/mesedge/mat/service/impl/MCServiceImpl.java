/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.mat.mapper.MaterialClassifyMapper;
import com.ourexists.mesedge.mat.pojo.MC;
import com.ourexists.mesedge.mat.model.query.MaterialClassifyPageQuery;
import com.ourexists.mesedge.mat.service.MCService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class MCServiceImpl extends AbstractMyBatisPlusService<MaterialClassifyMapper, MC>
        implements MCService {
    @Override
    public Page<MC> selectByPage(MaterialClassifyPageQuery dto) {
        LambdaQueryWrapper<MC> qw = new LambdaQueryWrapper<MC>()
                .eq(StringUtils.isNotEmpty(dto.getSelfCode()), MC::getSelfCode, dto.getSelfCode())
                .like(StringUtils.isNotEmpty(dto.getName()), MC::getName, dto.getName())
                .orderByDesc(MC::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }
}
