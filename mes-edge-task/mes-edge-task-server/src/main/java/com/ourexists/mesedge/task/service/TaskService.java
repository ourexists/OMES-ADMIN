/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.task.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.task.model.query.TaskPageQuery;
import com.ourexists.mesedge.task.pojo.Task;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface TaskService extends IMyBatisPlusService<Task> {

    Page<Task> selectByPage(TaskPageQuery dto);

    void addOrUpdate(Task wrap);

    void delete(String id);

    void start(String id);

    void stop(String id);


}
