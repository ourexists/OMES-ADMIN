/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.inspection.mapper.InspectPersonMapper;
import com.ourexists.omes.inspection.model.InspectPersonPageQuery;
import com.ourexists.omes.inspection.pojo.InspectPerson;
import com.ourexists.omes.inspection.service.InspectPersonService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class InspectPersonServiceImpl extends AbstractMyBatisPlusService<InspectPersonMapper, InspectPerson> implements InspectPersonService {

    @Override
    public Page<InspectPerson> selectByPage(InspectPersonPageQuery query) {
        LambdaQueryWrapper<InspectPerson> qw = new LambdaQueryWrapper<InspectPerson>()
                .like(StringUtils.hasText(query.getName()), InspectPerson::getName, query.getName())
                .eq(StringUtils.hasText(query.getJobNumber()), InspectPerson::getJobNumber, query.getJobNumber())
                .eq(StringUtils.hasText(query.getAccountId()), InspectPerson::getAccountId, query.getAccountId())
                .orderByDesc(InspectPerson::getId);
        return page(new Page<>(query.getPage(), query.getPageSize()), qw);
    }
}
