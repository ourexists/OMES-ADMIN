/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.inspection.mapper.InspectItemMapper;
import com.ourexists.omes.inspection.model.InspectItemPageQuery;
import com.ourexists.omes.inspection.pojo.InspectItem;
import com.ourexists.omes.inspection.service.InspectItemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InspectItemServiceImpl extends AbstractMyBatisPlusService<InspectItemMapper, InspectItem> implements InspectItemService {

    @Override
    public Page<InspectItem> selectByPage(InspectItemPageQuery query) {
        LambdaQueryWrapper<InspectItem> qw = new LambdaQueryWrapper<InspectItem>()
                .like(org.springframework.util.StringUtils.hasText(query.getItemName()), InspectItem::getItemName, query.getItemName())
                .eq(query.getItemType() != null, InspectItem::getItemType, query.getItemType())
                .orderByAsc(InspectItem::getId);
        return page(new Page<>(query.getPage(), query.getPageSize()), qw);
    }


    @Override
    public List<InspectItem> listAllPool() {
        return list(new LambdaQueryWrapper<InspectItem>()
                .orderByAsc(InspectItem::getId));
    }
}
