/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.mat.model.BOMDto;
import com.ourexists.mesedge.mat.mapper.BOMMapper;
import com.ourexists.mesedge.mat.pojo.BOM;
import com.ourexists.mesedge.mat.pojo.BOMD;
import com.ourexists.mesedge.mat.model.query.BOMPageQuery;
import com.ourexists.mesedge.mat.service.BOMDService;
import com.ourexists.mesedge.mat.service.BOMService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BOMServiceImpl extends AbstractMyBatisPlusService<BOMMapper, BOM> implements BOMService {

    @Autowired
    private BOMDService bomdService;

    @Override
    public Page<BOM> selectByPage(BOMPageQuery dto) {
        List<String> mcodes = null;
        if (StringUtils.isNotEmpty(dto.getDetailName())) {
            List<BOMD> mpsDetails = bomdService.list(new LambdaQueryWrapper<BOMD>().select(BOMD::getMcode).like(BOMD::getMatName, dto.getDetailName()));
            if (CollectionUtil.isBlank(mpsDetails)) {
                Page<BOM> page = new Page<>(dto.getPage(), dto.getPageSize());
                page.setRecords(new ArrayList<>());
                page.setTotal(0);
                return page;
            }
            mcodes = mpsDetails.stream().map(BOMD::getMcode).collect(Collectors.toList());
        }
        LambdaQueryWrapper<BOM> qw = new LambdaQueryWrapper<BOM>()
                .eq(dto.getType() != null, BOM::getType, dto.getType())
                .eq(StringUtils.isNotBlank(dto.getSelfCode()), BOM::getSelfCode, dto.getSelfCode())
                .eq(StringUtils.isNotBlank(dto.getClassifyCode()), BOM::getClassifyCode, dto.getClassifyCode())
                .in(CollectionUtil.isNotBlank(mcodes), BOM::getSelfCode, mcodes)
                .like(StringUtils.isNotEmpty(dto.getName()), BOM::getName, dto.getName())
                .orderByDesc(BOM::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    public void addOrUpdate(BOMDto dto) {
        saveOrUpdate(BOM.wrap(dto));
        if (CollectionUtil.isNotBlank(dto.getDetails())) {
            bomdService.remove(new LambdaQueryWrapper<BOMD>().eq(BOMD::getMcode, dto.getSelfCode()));
            bomdService.saveBatch(BOMD.wrap(dto.getDetails(), dto.getSelfCode()));
        }
    }
}
