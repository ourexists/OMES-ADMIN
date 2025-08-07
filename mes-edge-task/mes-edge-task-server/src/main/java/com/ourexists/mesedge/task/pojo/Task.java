/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.task.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import com.ourexists.mesedge.task.model.TaskDto;
import com.ourexists.mesedge.task.model.TaskVo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 配方表
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_task")
public class Task extends MainEntity {

    /**
     * 名称
     */
    private String name;


    /**
     * 任务类型
     */
    private String type;


    /**
     * cron表达式
     */
    private String cron;

    /**
     * 任务状态
     */
    private Integer status;

    public static TaskVo covert(Task source) {
        if (source == null) {
            return null;
        }
        TaskVo target = new TaskVo();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<TaskVo> covert(List<Task> sources) {
        List<TaskVo> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static <T extends TaskDto> Task wrap(T source) {
        Task target = new Task();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends TaskDto> List<Task> wrap(List<T> sources) {
        List<Task> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }
}
