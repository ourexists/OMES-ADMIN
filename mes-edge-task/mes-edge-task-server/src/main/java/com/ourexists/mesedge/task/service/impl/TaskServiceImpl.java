/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.task.enums.TaskStatusEnum;
import com.ourexists.mesedge.task.mapper.TaskMapper;
import com.ourexists.mesedge.task.model.query.TaskPageQuery;
import com.ourexists.mesedge.task.pojo.Task;
import com.ourexists.mesedge.task.process.TimerTaskManager;
import com.ourexists.mesedge.task.service.TaskService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskServiceImpl extends AbstractMyBatisPlusService<TaskMapper, Task>
        implements TaskService {

    private final TimerTaskManager timerTaskManager;

    public TaskServiceImpl(TimerTaskManager timerTaskManager) {
        this.timerTaskManager = timerTaskManager;
    }

    @PostConstruct
    public void init() {
        //初始化时加载所有的运行中的任务
        List<Task> taskList = this.list(new LambdaQueryWrapper<Task>().eq(Task::getStatus, TaskStatusEnum.RUNNING.getCode()));
        if (CollectionUtil.isNotBlank(taskList)) {
            for (Task task : taskList) {
                timerTaskManager.addTask(task.getId(), task.getType(), task.getCron());
            }
        }
    }

    @Override
    public Page<Task> selectByPage(TaskPageQuery dto) {
        LambdaQueryWrapper<Task> qw = new LambdaQueryWrapper<Task>()
                .orderByDesc(Task::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdate(Task task) {
        if (task.getId() == null) {
            task.setId(IdWorker.getIdStr());
        } else {
            Task task1 = this.getById(task.getId());
            if (task1.getStatus().equals(TaskStatusEnum.RUNNING.getCode())) {
                throw new BusinessException("Task is Running, Cannot be update");
            }
        }
        if (task.getStatus() != null && task.getStatus().equals(TaskStatusEnum.RUNNING.getCode())) {
            timerTaskManager.updateTask(task.getId(), task.getType(), task.getCron());
        }
        this.saveOrUpdate(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        timerTaskManager.removeTask(id);
        this.removeById(id);
    }

    @Override
    public void start(String id) {
        if (this.update(
                new LambdaUpdateWrapper<Task>()
                        .set(Task::getStatus, TaskStatusEnum.RUNNING.getCode())
                        .eq(Task::getId, id)
                        .eq(Task::getStatus, TaskStatusEnum.STOPPING.getCode()))) {
            Task task = this.getById(id);
            timerTaskManager.addTask(task.getId(), task.getType(), task.getCron());
        }
    }

    @Override
    public void stop(String id) {
        if (this.update(
                new LambdaUpdateWrapper<Task>()
                        .set(Task::getStatus, TaskStatusEnum.STOPPING.getCode())
                        .eq(Task::getId, id)
                        .eq(Task::getStatus, TaskStatusEnum.RUNNING.getCode()))) {
            timerTaskManager.removeTask(id);
        }
    }
}
