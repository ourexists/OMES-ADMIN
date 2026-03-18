/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.inspection.enums.InspectTaskStatusEnum;
import com.ourexists.omes.inspection.mapper.InspectTaskMapper;
import com.ourexists.omes.inspection.model.InspectTaskPageQuery;
import com.ourexists.omes.inspection.pojo.InspectPlan;
import com.ourexists.omes.inspection.pojo.InspectTask;
import com.ourexists.omes.inspection.service.InspectPlanService;
import com.ourexists.omes.inspection.service.InspectTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class InspectTaskServiceImpl extends AbstractMyBatisPlusService<InspectTaskMapper, InspectTask> implements InspectTaskService {

    @Autowired
    private InspectPlanService inspectPlanService;

    @Override
    public Page<InspectTask> selectByPage(InspectTaskPageQuery query) {
        LambdaQueryWrapper<InspectTask> qw = new LambdaQueryWrapper<InspectTask>()
                .eq(StringUtils.hasText(query.getPlanId()), InspectTask::getPlanId, query.getPlanId())
                .eq(query.getStatus() != null, InspectTask::getStatus, query.getStatus())
                .eq(StringUtils.hasText(query.getExecutorId()), InspectTask::getExecutorId, query.getExecutorId())
                .and(Boolean.TRUE.equals(query.getUnassigned()), w -> w.isNull(InspectTask::getExecutorPersonId).or().eq(InspectTask::getExecutorPersonId, ""))
                .ge(query.getScheduledTimeStart() != null, InspectTask::getScheduledTime, query.getScheduledTimeStart())
                .le(query.getScheduledTimeEnd() != null, InspectTask::getScheduledTime, query.getScheduledTimeEnd())
                .orderByDesc(InspectTask::getScheduledTime);
        if (StringUtils.hasText(query.getPlanName())) {
            List<String> planIds = inspectPlanService.list(new LambdaQueryWrapper<InspectPlan>()
                            .like(InspectPlan::getName, query.getPlanName())
                            .select(InspectPlan::getId)).stream()
                    .map(InspectPlan::getId).toList();
            if (CollectionUtils.isEmpty(planIds)) {
                return new Page<>(query.getPage(), query.getPageSize(), 0);
            }
            qw.in(InspectTask::getPlanId, planIds);
        }
        if (!CollectionUtils.isEmpty(query.getWorkshopCodes())) {
            List<String> planIdsByWorkshop = inspectPlanService.list(new LambdaQueryWrapper<InspectPlan>()
                            .in(InspectPlan::getWorkshopCode, query.getWorkshopCodes())
                            .select(InspectPlan::getId)).stream()
                    .map(InspectPlan::getId).toList();
            if (CollectionUtils.isEmpty(planIdsByWorkshop)) {
                return new Page<>(query.getPage(), query.getPageSize(), 0);
            }
            qw.in(InspectTask::getPlanId, planIdsByWorkshop);
        }
        return page(new Page<>(query.getPage(), query.getPageSize()), qw);
    }

    @Override
    public List<InspectTask> listByPlanId(String planId) {
        return list(new LambdaQueryWrapper<InspectTask>().eq(InspectTask::getPlanId, planId).orderByDesc(InspectTask::getScheduledTime));
    }

    @Override
    public int markOverdueTasks() {
        // 计划执行日期早于当前日期的待执行任务标记为已逾期（MySQL：DATE(scheduled_time) < CURDATE()）
        LambdaUpdateWrapper<InspectTask> uw = new LambdaUpdateWrapper<InspectTask>()
                .eq(InspectTask::getStatus, InspectTaskStatusEnum.PENDING.getCode())
                .apply("DATE(scheduled_time) < CURDATE()")
                .set(InspectTask::getStatus, InspectTaskStatusEnum.OVERDUE.getCode());
        return Math.toIntExact(baseMapper.update(null, uw));
    }

    @Override
    public int restartOverdue(List<String> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) return 0;
        LambdaUpdateWrapper<InspectTask> uw = new LambdaUpdateWrapper<InspectTask>()
                .in(InspectTask::getId, taskIds)
                .eq(InspectTask::getStatus, InspectTaskStatusEnum.OVERDUE.getCode())
                .set(InspectTask::getStatus, InspectTaskStatusEnum.PENDING.getCode());
        return Math.toIntExact(baseMapper.update(null, uw));
    }
}
