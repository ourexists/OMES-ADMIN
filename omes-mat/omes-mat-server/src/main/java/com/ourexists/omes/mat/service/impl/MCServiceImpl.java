/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.mat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.mat.mapper.MaterialClassifyMapper;
import com.ourexists.omes.mat.pojo.MAT;
import com.ourexists.omes.mat.pojo.MC;
import com.ourexists.omes.mat.model.query.MaterialClassifyPageQuery;
import com.ourexists.omes.mat.service.MATService;
import com.ourexists.omes.mat.service.MCService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MCServiceImpl extends AbstractMyBatisPlusService<MaterialClassifyMapper, MC>
        implements MCService {
    @Autowired
    private MATService matService;


    @Override
    public Page<MC> selectByPage(MaterialClassifyPageQuery dto) {
        LambdaQueryWrapper<MC> qw = new LambdaQueryWrapper<MC>()
                .eq(StringUtils.isNotEmpty(dto.getSelfCode()), MC::getSelfCode, dto.getSelfCode())
                .like(StringUtils.isNotEmpty(dto.getName()), MC::getName, dto.getName())
                .orderByDesc(MC::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        List<MC> mcs = this.listByIds(ids);
        if (CollectionUtil.isBlank(mcs)) {
            return;
        }
        List<String> codes = mcs.stream().map(MC::getSelfCode).collect(Collectors.toList());
        long c = matService.count(new LambdaQueryWrapper<MAT>().in(MAT::getClassifyCode, codes));
        if (c > 0) {
            throw new BusinessException("${common.msg.date.use}");
        }
        this.removeByIds(ids);
    }
}
