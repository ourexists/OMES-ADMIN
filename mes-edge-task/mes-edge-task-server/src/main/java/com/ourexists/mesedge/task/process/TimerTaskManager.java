/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.task.process;

import com.ourexists.mesedge.task.process.task.TimerTask;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
public class TimerTaskManager {

    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final Map<String, ScheduledFuture<?>> taskMap = new ConcurrentHashMap<>();

    private Map<String, TimerTask> timerTaskMap;

    public TimerTaskManager(Map<String, TimerTask> timerTaskMap) {
        this.timerTaskMap = new ConcurrentHashMap<>();
        if (timerTaskMap != null && !timerTaskMap.isEmpty()) {
            this.timerTaskMap.putAll(timerTaskMap);
        }
    }

    @PostConstruct
    public void init() {
        scheduler.setPoolSize(10);
        scheduler.initialize();
    }

    /**
     * 获取所有的任务
     *
     * @return
     */
    public Map<String, TimerTask> getAllTimerTask() {
        return timerTaskMap;
    }

    /**
     * 添加定时任务
     */
    public void addTask(String taskId, String timerTask, String cron) {
        TimerTask task = timerTaskMap.get(timerTask);
        if (task == null) {
            return;
        }
        addTask(taskId, task, cron);
    }

    // 添加定时任务
    public void addTask(String taskId, TimerTask timerTask, String cron) {
        ScheduledFuture<?> future = scheduler.schedule(timerTask, new CronTrigger(cron));
        taskMap.put(taskId, future);
    }

    // 移除定时任务
    public void removeTask(String taskId) {
        ScheduledFuture<?> future = taskMap.remove(taskId);
        if (future != null) {
            future.cancel(true);
        }
    }

    /**
     * 修改定时任务
     *
     * @param taskId
     * @param timerTask
     * @param newCron
     */
    public void updateTask(String taskId, String timerTask, String newCron) {
        TimerTask task = timerTaskMap.get(timerTask);
        if (task == null) {
            return;
        }
        updateTask(taskId, task, newCron);
    }

    /**
     * 修改定时任务
     *
     * @param taskId
     * @param timerTask
     * @param newCron
     */
    public void updateTask(String taskId, TimerTask timerTask, String newCron) {
        removeTask(taskId);
        addTask(taskId, timerTask, newCron);
    }
}
