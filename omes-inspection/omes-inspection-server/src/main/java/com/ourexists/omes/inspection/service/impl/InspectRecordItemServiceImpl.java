/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.inspection.mapper.InspectRecordItemMapper;
import com.ourexists.omes.inspection.pojo.InspectRecordItem;
import com.ourexists.omes.inspection.service.InspectRecordItemService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class InspectRecordItemServiceImpl extends AbstractMyBatisPlusService<InspectRecordItemMapper, InspectRecordItem> implements InspectRecordItemService {

    @Override
    public List<InspectRecordItem> listByRecordIds(List<String> recordIds) {
        if (CollectionUtils.isEmpty(recordIds)) {
            return new ArrayList<>();
        }
        return list(new LambdaQueryWrapper<InspectRecordItem>()
                .in(InspectRecordItem::getRecordId, recordIds)
                .orderByAsc(InspectRecordItem::getRecordId));
    }
}
