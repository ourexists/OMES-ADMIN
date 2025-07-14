/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.device.mapper.DevgMapper;
import com.ourexists.mesedge.device.model.DevgDto;
import com.ourexists.mesedge.device.model.DevgPageQuery;
import com.ourexists.mesedge.device.pojo.Devg;
import com.ourexists.mesedge.device.service.DevgService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DevgServiceImpl extends AbstractMyBatisPlusService<DevgMapper, Devg> implements DevgService {

    @Override
    public Page<Devg> selectByPage(DevgPageQuery dto) {
        List<String> mcodes = null;
        LambdaQueryWrapper<Devg> qw = new LambdaQueryWrapper<Devg>()
                .eq(StringUtils.isNotBlank(dto.getSelfCode()), Devg::getSelfCode, dto.getSelfCode())
                .in(CollectionUtil.isNotBlank(mcodes), Devg::getSelfCode, mcodes)
                .like(StringUtils.isNotEmpty(dto.getName()), Devg::getName, dto.getName())
                .orderByDesc(Devg::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    public void addOrUpdate(DevgDto dto) {
        saveOrUpdate(Devg.wrap(dto));
    }
}
