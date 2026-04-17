/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.device.mapper.EquipCollectMapper;
import com.ourexists.omes.device.model.EquipCollectDto;
import com.ourexists.omes.device.model.EquipCollectPageQuery;
import com.ourexists.omes.device.pojo.EquipCollect;
import com.ourexists.omes.device.service.EquipCollectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class EquipCollectServiceImpl extends AbstractMyBatisPlusService<EquipCollectMapper, EquipCollect>
        implements EquipCollectService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatch(Collection<EquipCollect> entityList, int batchSize) {
        if (CollectionUtils.isEmpty(entityList)) {
            return false;
        }
        List<EquipCollect> all = new ArrayList<>(entityList);
        int size = all.size();
        int chunk = Math.max(batchSize, 1);
        for (int i = 0; i < size; i += chunk) {
            int end = Math.min(i + chunk, size);
            List<EquipCollect> part = all.subList(i, end);
            this.baseMapper.insertBatchWithJsonb(part);
        }
        return true;
    }

    @Override
    public Page<EquipCollect> selectByPage(EquipCollectPageQuery dto) {
        LambdaQueryWrapper<EquipCollect> qw = new LambdaQueryWrapper<EquipCollect>()
                .eq(StringUtils.hasText(dto.getSn()), EquipCollect::getSn, dto.getSn())
                .between(dto.getStartDate() != null && dto.getEndDate() != null, EquipCollect::getTime, dto.getStartDate(), dto.getEndDate())
                .orderByAsc(EquipCollect::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    public void addOrUpdate(EquipCollectDto dto) {
        saveOrUpdate(EquipCollect.wrap(dto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        this.removeBatchByIds(ids);
    }

    @Override
    public List<EquipCollect> queryByEquip(List<String> sns) {
        return this.list(new LambdaUpdateWrapper<EquipCollect>().in(EquipCollect::getSn, sns));
    }

    @Override
    public EquipCollect queryByEquip(String sn) {
        return this.getOne(new LambdaUpdateWrapper<EquipCollect>().eq(EquipCollect::getSn, sn));
    }
}
