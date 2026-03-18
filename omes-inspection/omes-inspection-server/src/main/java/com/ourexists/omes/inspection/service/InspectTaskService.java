/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.inspection.model.InspectTaskPageQuery;
import com.ourexists.omes.inspection.pojo.InspectTask;

import java.util.List;

public interface InspectTaskService extends IMyBatisPlusService<InspectTask> {

    Page<InspectTask> selectByPage(InspectTaskPageQuery query);

    List<InspectTask> listByPlanId(String planId);

    /**
     * 将计划执行日期已过且仍为「待执行」的任务标记为「已逾期」。
     * 规则：scheduledTime 所在日期早于当前日期的待执行任务视为逾期。
     */
    int markOverdueTasks();

    /**
     * 将已逾期的任务重置为待执行（仅当状态为已逾期时更新）。
     * @param taskIds 任务ID列表
     * @return 实际更新的条数
     */
    int restartOverdue(java.util.List<String> taskIds);
}
