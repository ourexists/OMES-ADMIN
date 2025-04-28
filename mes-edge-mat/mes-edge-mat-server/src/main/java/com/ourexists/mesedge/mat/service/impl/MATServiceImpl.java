/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.mat.mapper.MaterialMapper;
import com.ourexists.mesedge.mat.pojo.MAT;
import com.ourexists.mesedge.mat.model.query.MaterialPageQuery;
import com.ourexists.mesedge.mat.service.MATService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class MATServiceImpl extends AbstractMyBatisPlusService<MaterialMapper, MAT> implements MATService {
    @Override
    public Page<MAT> selectByPage(MaterialPageQuery dto) {
        LambdaQueryWrapper<MAT> qw = new LambdaQueryWrapper<MAT>()
                .eq(StringUtils.isNotEmpty(dto.getSelfCode()), MAT::getSelfCode, dto.getSelfCode())
                .eq(StringUtils.isNotEmpty(dto.getClassifyCode()), MAT::getClassifyCode, dto.getClassifyCode())
                .like(StringUtils.isNotEmpty(dto.getName()), MAT::getName, dto.getName())
                .orderByDesc(MAT::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }
}
