/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.inspection.mapper.InspectTemplateMapper;
import com.ourexists.omes.inspection.model.InspectTemplatePageQuery;
import com.ourexists.omes.inspection.pojo.InspectTemplate;
import com.ourexists.omes.inspection.service.InspectTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class InspectTemplateServiceImpl extends AbstractMyBatisPlusService<InspectTemplateMapper, InspectTemplate> implements InspectTemplateService {

    @Override
    public Page<InspectTemplate> selectByPage(InspectTemplatePageQuery query) {
        LambdaQueryWrapper<InspectTemplate> qw = new LambdaQueryWrapper<InspectTemplate>()
                .like(StringUtils.hasText(query.getName()), InspectTemplate::getName, query.getName())
                .orderByDesc(InspectTemplate::getId);
        return page(new Page<>(query.getPage(), query.getPageSize()), qw);
    }

    @Override
    public List<InspectTemplate> selectList() {
        return list(new LambdaQueryWrapper<InspectTemplate>().orderByAsc(InspectTemplate::getName));
    }
}
