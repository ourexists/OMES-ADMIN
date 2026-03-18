/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.omes.inspection.model.InspectPlanDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 巡检计划
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName(value = "t_inspect_plan", autoResultMap = true)
public class InspectPlan extends EraEntity {

    private String name;

    /** 关联巡检模板ID */
    private String templateId;

    /** 周期类型：1每日 2每周 3每月 */
    private Integer cycleType;

    /** 周期配置 */
    private String cycleConfig;

    private String workshopCode;

    /** 关联设备ID列表 JSON */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> equipIds;

    /** 状态：0禁用 1启用 */
    private Integer status;

    private String remark;

    public static InspectPlanDto covert(InspectPlan source) {
        if (source == null) return null;
        InspectPlanDto target = new InspectPlanDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<InspectPlanDto> covert(List<InspectPlan> sources) {
        List<InspectPlanDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (InspectPlan source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static <T extends InspectPlanDto> InspectPlan wrap(T source) {
        if (source == null) return null;
        InspectPlan target = new InspectPlan();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends InspectPlanDto> List<InspectPlan> wrap(List<T> sources) {
        List<InspectPlan> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
