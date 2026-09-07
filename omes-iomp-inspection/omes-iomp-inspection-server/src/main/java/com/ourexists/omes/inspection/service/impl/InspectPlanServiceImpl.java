/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.inspection.manager.InspectPlanTaskManager;
import com.ourexists.omes.inspection.mapper.InspectPlanMapper;
import com.ourexists.omes.inspection.model.InspectPlanPageQuery;
import com.ourexists.omes.inspection.pojo.InspectPlan;
import com.ourexists.omes.inspection.service.InspectPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class InspectPlanServiceImpl extends AbstractMyBatisPlusService<InspectPlanMapper, InspectPlan> implements InspectPlanService {

    @Autowired
    private InspectPlanTaskManager inspectPlanTaskManager;

    @Override
    public Page<InspectPlan> selectByPage(InspectPlanPageQuery query) {
        LambdaQueryWrapper<InspectPlan> qw = new LambdaQueryWrapper<InspectPlan>()
                .like(StringUtils.hasText(query.getName()), InspectPlan::getName, query.getName())
                .eq(StringUtils.hasText(query.getTemplateId()), InspectPlan::getTemplateId, query.getTemplateId())
                .eq(query.getCycleType() != null, InspectPlan::getCycleType, query.getCycleType())
                .eq(StringUtils.hasText(query.getWorkshopCode()), InspectPlan::getWorkshopCode, query.getWorkshopCode())
                .eq(query.getStatus() != null, InspectPlan::getStatus, query.getStatus())
                .in(!CollectionUtils.isEmpty(query.getWorkshopCodes()), InspectPlan::getWorkshopCode, query.getWorkshopCodes())
                .orderByDesc(InspectPlan::getId);
        return page(new Page<>(query.getPage(), query.getPageSize()), qw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enable(String planId) {
        if (!StringUtils.hasText(planId)) return;
        InspectPlan plan = getById(planId);
        if (plan == null) return;
        plan.setStatus(1);
        updateById(plan);
        inspectPlanTaskManager.registerPlan(planId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(String planId) {
        if (!StringUtils.hasText(planId)) return;
        InspectPlan plan = getById(planId);
        if (plan == null) return;
        plan.setStatus(0);
        updateById(plan);
        inspectPlanTaskManager.removePlan(planId);
    }

    @Override
    public void generateTasks(String planId) {
        inspectPlanTaskManager.generateTasks(planId);
    }
}
