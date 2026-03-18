/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 *
 * 巡检任务管理器：独立管理各启用计划的定时任务，到点生成巡检任务记录。
 */
package com.ourexists.omes.inspection.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.omes.inspection.enums.InspectPlanCycleTypeEnum;
import com.ourexists.omes.inspection.pojo.InspectPlan;
import com.ourexists.omes.inspection.enums.InspectTaskStatusEnum;
import com.ourexists.omes.inspection.pojo.InspectTask;
import com.ourexists.omes.inspection.service.InspectPlanService;
import com.ourexists.omes.inspection.service.InspectTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
public class InspectPlanTaskManager implements InitializingBean {

    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final ConcurrentHashMap<String, ScheduledFuture<?>> planIdToFuture = new ConcurrentHashMap<>();

    @Autowired
    private InspectPlanService inspectPlanService;
    @Autowired
    private InspectTaskService inspectTaskService;

    @Override
    public void afterPropertiesSet() {
        UserContext.defaultTenant();
        scheduler.setPoolSize(8);
        scheduler.setThreadNamePrefix("inspect-plan-");
        scheduler.initialize();
        // 定时将逾期的待执行任务标记为已逾期：每天 00:05 执行
        scheduler.schedule(this::runMarkOverdue, new CronTrigger("0 5 0 * * ?"));
        // 启动时执行一次逾期标记，避免长时间未重启导致大量未标记逾期
        scheduler.schedule(this::runMarkOverdue, new Date(System.currentTimeMillis() + 5000));
        List<InspectPlan> enabled = inspectPlanService.list(
                new LambdaQueryWrapper<InspectPlan>()
                        .eq(InspectPlan::getStatus, 1)
        );
        if (CollectionUtil.isBlank(enabled)) {
            return;
        }
        // 启动时把已启用的计划重新注册到管理器
        for (InspectPlan plan : enabled) {
            registerPlan(plan.getId());
        }
    }

    /** 扫描并标记逾期任务（计划执行日期已过且仍为待执行 -> 已逾期） */
    protected void runMarkOverdue() {
        try {
            UserContext.defaultTenant();
            int count = inspectTaskService.markOverdueTasks();
            if (count > 0) {
                log.info("inspect task overdue marked: count={}", count);
            }
        } catch (Exception e) {
            log.error("inspect task mark overdue error", e);
        }
    }

    /**
     * 启用计划：更新状态并在本管理器中注册定时任务，到点生成巡检任务。
     */
    public void registerPlan(String planId) {
        removePlan(planId);
        InspectPlan plan = inspectPlanService.getById(planId);
        if (plan == null || plan.getStatus() == null || plan.getStatus() != 1) {
            log.debug("inspect plan not found or not enabled, skip register: planId={}", planId);
            return;
        }
        String cron = buildCron(plan.getCycleType(), plan.getCycleConfig());
        if (cron == null) {
            log.warn("inspect plan cron invalid, skip register: planId={}, cycleType={}, cycleConfig={}",
                    planId, plan.getCycleType(), plan.getCycleConfig());
            return;
        }
        Runnable job = () -> runPlan(planId, true);
        ScheduledFuture<?> future = scheduler.schedule(job, new CronTrigger(cron));
        planIdToFuture.put(planId, future);
        log.info("inspect plan task registered: planId={}, cron={}", planId, cron);
    }

    /**
     * 停用计划：从管理器中移除定时任务。
     */
    public void removePlan(String planId) {
        ScheduledFuture<?> future = planIdToFuture.remove(planId);
        if (future != null) {
            future.cancel(true);
            log.info("inspect plan task removed: planId={}", planId);
        }
    }

    /**
     * 手动触发：根据计划立即生成巡检任务（不校验计划是否启用）。
     */
    public void generateTasks(String planId) {
        runPlan(planId, false);
    }

    /**
     * 到点执行：根据计划生成巡检任务。
     * 若计划选择了多个场景（workshopCode 逗号分隔），则按场景各生成一条任务；否则生成一条任务。
     * @param onlyWhenEnabled true 时仅当计划已启用才生成，false 时不校验状态（用于手动触发）
     */
    protected void runPlan(String planId, boolean onlyWhenEnabled) {
        try {
            InspectPlan plan = inspectPlanService.getById(planId);
            if (plan == null) {
                log.warn("inspect plan not found, skip generate: planId={}", planId);
                return;
            }
            if (onlyWhenEnabled && (plan.getStatus() == null || plan.getStatus() != 1)) {
                log.debug("inspect plan not enabled, skip generate: planId={}", planId);
                return;
            }
            Date now = new Date();
            List<String> workshopCodes = parseWorkshopCodes(plan.getWorkshopCode());
            String templateId = StringUtils.hasText(plan.getTemplateId()) ? plan.getTemplateId() : null;
            List<InspectTask> tasks = new ArrayList<>();
            if (CollectionUtil.isBlank(workshopCodes)) {
                InspectTask task = new InspectTask();
                task.setPlanId(planId);
                task.setTemplateId(templateId);
                task.setScheduledTime(now);
                task.setStatus(InspectTaskStatusEnum.PENDING.getCode());
                tasks.add(task);
            } else {
                for (String code : workshopCodes) {
                    InspectTask task = new InspectTask();
                    task.setPlanId(planId);
                    task.setTemplateId(templateId);
                    task.setScheduledTime(now);
                    task.setStatus(InspectTaskStatusEnum.PENDING.getCode());
                    task.setWorkshopCode(code);
                    tasks.add(task);
                }
            }
            if (!tasks.isEmpty()) {
                inspectTaskService.saveBatch(tasks);
                log.debug("inspect tasks created: planId={}, count={}", planId, tasks.size());
            }
        } catch (Exception e) {
            log.error("inspect plan run error: planId={}", planId, e);
        }
    }

    /** 解析计划中的场景编号：逗号分隔，去空；无或空则返回空列表。 */
    private List<String> parseWorkshopCodes(String workshopCode) {
        if (!StringUtils.hasText(workshopCode)) {
            return new ArrayList<>();
        }
        List<String> list = new ArrayList<>();
        for (String s : workshopCode.split(",")) {
            String t = s.trim();
            if (StringUtils.hasText(t)) {
                list.add(t);
            }
        }
        return list;
    }

    /**
     * 周期类型 + 周期配置 -> Quartz cron。
     * 每日: cycleConfig=09:00  -> 0 0 9 * * ?
     * 每周: cycleConfig=1,09:00 (1=周一) -> 0 0 9 ? * 2
     * 每月: cycleConfig=1,09:00 (1号) -> 0 0 9 1 * ?
     */
    private String buildCron(Integer cycleType, String cycleConfig) {
        if (cycleType == null || !StringUtils.hasText(cycleConfig)) {
            return null;
        }
        String trimmed = cycleConfig.trim();
        int type = cycleType.intValue();
        if (type == InspectPlanCycleTypeEnum.DAILY.getCode()) {
            // 每日: 09:00
            int[] hm = parseTime(trimmed);
            if (hm == null) return null;
            return String.format("0 %d %d * * ?", hm[1], hm[0]);
        }
        if (type == InspectPlanCycleTypeEnum.WEEKLY.getCode()) {
            // 每周: 1,09:00  (1=周一，Quartz 2=周一)
            int comma = trimmed.indexOf(',');
            if (comma <= 0) return null;
            int dayOfWeek = parseDayOfWeek(trimmed.substring(0, comma).trim());
            if (dayOfWeek < 0) return null;
            int[] hm = parseTime(trimmed.substring(comma + 1).trim());
            if (hm == null) return null;
            return String.format("0 %d %d ? * %d", hm[1], hm[0], dayOfWeek);
        }
        if (type == InspectPlanCycleTypeEnum.MONTHLY.getCode()) {
            // 每月: 1,09:00 (1号)
            int comma = trimmed.indexOf(',');
            if (comma <= 0) return null;
            int day = parseDayOfMonth(trimmed.substring(0, comma).trim());
            if (day < 1 || day > 31) return null;
            int[] hm = parseTime(trimmed.substring(comma + 1).trim());
            if (hm == null) return null;
            return String.format("0 %d %d %d * ?", hm[1], hm[0], day);
        }
        return null;
    }

    private int[] parseTime(String timeStr) {
        if (!StringUtils.hasText(timeStr)) return null;
        String s = timeStr.trim();
        int colon = s.indexOf(':');
        if (colon <= 0) return null;
        try {
            int hour = Integer.parseInt(s.substring(0, colon).trim(), 10);
            int minute = s.length() > colon + 1 ? Integer.parseInt(s.substring(colon + 1).trim(), 10) : 0;
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return null;
            return new int[]{hour, minute};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 周一=1 -> Quartz 2, 周日=7 -> Quartz 1 */
    private int parseDayOfWeek(String s) {
        try {
            int d = Integer.parseInt(s, 10);
            if (d >= 1 && d <= 7) return d == 7 ? 1 : d + 1;
            return -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int parseDayOfMonth(String s) {
        try {
            return Integer.parseInt(s, 10);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
