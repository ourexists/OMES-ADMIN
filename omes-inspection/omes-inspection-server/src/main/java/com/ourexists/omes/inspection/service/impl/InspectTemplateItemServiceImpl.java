/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.inspection.mapper.InspectTemplateItemMapper;
import com.ourexists.omes.inspection.pojo.InspectTemplateItem;
import com.ourexists.omes.inspection.service.InspectTemplateItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class InspectTemplateItemServiceImpl extends AbstractMyBatisPlusService<InspectTemplateItemMapper, InspectTemplateItem> implements InspectTemplateItemService {

    @Override
    public List<InspectTemplateItem> listByTemplateId(String templateId) {
        if (!StringUtils.hasText(templateId)) return List.of();
        return list(new LambdaQueryWrapper<InspectTemplateItem>()
                .eq(InspectTemplateItem::getTemplateId, templateId)
                .orderByAsc(InspectTemplateItem::getProductCode, InspectTemplateItem::getSortOrder, InspectTemplateItem::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBatch(String templateId, List<InspectTemplateItem> items) {
        if (!StringUtils.hasText(templateId)) return;
        deleteByTemplateId(templateId);
        if (CollectionUtils.isEmpty(items)) return;
        int order = 0;
        for (InspectTemplateItem item : items) {
            if (item.getId() == null || item.getId().isBlank()) {
                item.setId(UUID.randomUUID().toString().replace("-", ""));
            }
            item.setTemplateId(templateId);
            if (item.getSortOrder() == null) item.setSortOrder(order++);
            save(item);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByTemplateId(String templateId) {
        if (!StringUtils.hasText(templateId)) return;
        remove(new LambdaQueryWrapper<InspectTemplateItem>().eq(InspectTemplateItem::getTemplateId, templateId));
    }
}

