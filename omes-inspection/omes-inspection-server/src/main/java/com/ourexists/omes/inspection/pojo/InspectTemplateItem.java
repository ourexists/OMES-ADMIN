/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.omes.inspection.model.InspectTemplateItemDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 模板-巡检项关联（某模板下某产品块的一条巡检项）
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_inspect_template_item")
public class InspectTemplateItem extends EraEntity {

    /** 所属模板ID */
    private String templateId;
    /** 所属产品编号（模板中的产品块） */
    private String productCode;
    /** 引用的巡检项ID（从巡检项池载入时绑定） */
    private String referenceItemId;
    /** 排序号 */
    private Integer sortOrder;
    /** 权重（模板内该项的权重） */
    private Integer weight;
    /** 百分比权重（该项权重/同产品块权重总值，0~1 小数如 0.3） */
    private java.math.BigDecimal weightRate;
    /** 规则配置 JSON，如数值区间/是否/选项与分值 */
    private String ruleConfig;

    public static InspectTemplateItemDto covert(InspectTemplateItem source) {
        if (source == null) return null;
        InspectTemplateItemDto target = new InspectTemplateItemDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<InspectTemplateItemDto> covert(List<InspectTemplateItem> sources) {
        List<InspectTemplateItemDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (InspectTemplateItem source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static <T extends InspectTemplateItemDto> InspectTemplateItem wrap(T source) {
        if (source == null) return null;
        InspectTemplateItem target = new InspectTemplateItem();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends InspectTemplateItemDto> List<InspectTemplateItem> wrap(List<T> sources) {
        List<InspectTemplateItem> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}

