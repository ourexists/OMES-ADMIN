/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.message.timer;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ourexists.mesedge.core.NotifyPusher;
import com.ourexists.mesedge.message.enums.NotifyStatusEnum;
import com.ourexists.mesedge.message.pojo.Notify;
import com.ourexists.mesedge.message.service.NotifyService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
public class NotifyTimerManager {

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private NotifyPusher notifyPusher;

    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final Map<String, ScheduledFuture<?>> taskMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        scheduler.setPoolSize(50);
        scheduler.initialize();

        List<Notify> notifyList = notifyService.list(new LambdaUpdateWrapper<Notify>().eq(Notify::getStatus, NotifyStatusEnum.PROGRESS.getCode()));
        if (!CollectionUtils.isEmpty(notifyList)) {
            notifyList.forEach(notify -> {
                addTask(notify.getId(), notify.getStep());
            });
        }
    }

    /**
     * 添加定时任务
     */
    public void addTask(String taskId, Integer seconds) {
        String cron = "0 */" + seconds + " * * * ?";
        addTask(taskId, cron);
    }

    // 添加定时任务
    public void addTask(String taskId, String cron) {
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            Notify notify = notifyService.getById(taskId);
            if (notify != null) {
                notifyPusher.push(Notify.covertMsg(notify));
            }
        }, new CronTrigger(cron));
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
     */
    public void updateTask(String taskId, Integer seconds) {
        String cron = "*/" + seconds + " * * * * ?";
        updateTask(taskId, cron);
    }

    /**
     * 修改定时任务
     *
     * @param taskId
     * @param newCron
     */
    public void updateTask(String taskId, String newCron) {
        removeTask(taskId);
        addTask(taskId, newCron);
    }
}
