/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.ec.mapper.EcAttrMapper;
import com.ourexists.mesedge.ec.model.EcAttrBatchDto;
import com.ourexists.mesedge.ec.model.EcAttrDto;
import com.ourexists.mesedge.ec.model.EcAttrPageQuery;
import com.ourexists.mesedge.ec.pojo.EcAttr;
import com.ourexists.mesedge.ec.service.EcAttrService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class EcAttrServiceImpl extends AbstractMyBatisPlusService<EcAttrMapper, EcAttr> implements EcAttrService {

    @Override
    public Page<EcAttr> selectByPage(EcAttrPageQuery dto) {
        LambdaQueryWrapper<EcAttr> qw = new LambdaQueryWrapper<EcAttr>()
                .eq(StringUtils.hasText(dto.getWorkshopId()), EcAttr::getWorkshopId, dto.getWorkshopId())
                .orderByAsc(EcAttr::getSort, EcAttr::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    public void addOrUpdate(EcAttrDto dto) {
        saveOrUpdate(EcAttr.wrap(dto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        this.removeBatchByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertBatch(EcAttrBatchDto dto) {
        this.remove(new LambdaUpdateWrapper<EcAttr>().eq(EcAttr::getWorkshopId, dto.getWorkshopId()));
        if (CollectionUtils.isEmpty(dto.getEcAttrs())) {
            return;
        }
        dto.getEcAttrs().forEach(ecAttr -> {
            ecAttr.setWorkshopId(dto.getWorkshopId());
        });
        this.saveBatch(EcAttr.wrap(dto.getEcAttrs()));
    }
}
