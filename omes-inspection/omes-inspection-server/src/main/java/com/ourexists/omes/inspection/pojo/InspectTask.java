/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.omes.inspection.model.InspectTaskDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 巡检任务
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_inspect_task")
public class InspectTask extends EraEntity {

    private String planId;
    /** 关联巡检模板ID（来自计划） */
    private String templateId;
    private Date scheduledTime;
    /** 状态：0待执行 1执行中 2已完成 3已逾期 */
    private Integer status;
    /** 指派的巡检人员ID */
    private String executorPersonId;
    private String executorId;
    private String executorName;
    private Date actualStartTime;
    private Date actualEndTime;
    /** 关联单个场景编号，为空表示按计划范围 */
    private String workshopCode;
    private String remark;

    public static InspectTaskDto covert(InspectTask source) {
        if (source == null) return null;
        InspectTaskDto target = new InspectTaskDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<InspectTaskDto> covert(List<InspectTask> sources) {
        List<InspectTaskDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (InspectTask source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static <T extends InspectTaskDto> InspectTask wrap(T source) {
        if (source == null) return null;
        InspectTask target = new InspectTask();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends InspectTaskDto> List<InspectTask> wrap(List<T> sources) {
        List<InspectTask> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
