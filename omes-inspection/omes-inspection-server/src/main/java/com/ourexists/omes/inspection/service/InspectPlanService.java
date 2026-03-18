/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.inspection.model.InspectPlanPageQuery;
import com.ourexists.omes.inspection.pojo.InspectPlan;

public interface InspectPlanService extends IMyBatisPlusService<InspectPlan> {

    Page<InspectPlan> selectByPage(InspectPlanPageQuery query);

    /** 启用计划：状态改为启用并注册定时任务 */
    void enable(String planId);

    /** 停用计划：状态改为禁用并移除定时任务 */
    void disable(String planId);

    /** 手动根据计划生成巡检任务（与定时到点逻辑一致） */
    void generateTasks(String planId);
}
