/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.omes.inspection.model.InspectItemDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 巡检项（模板下的单条：巡检项名称、类型、单位）
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_inspect_item")
public class InspectItem extends EraEntity {

    /** 巡检项名称 */
    private String itemName;
    /** 类型：1选择 2数值 3是否 */
    private Integer itemType;
    /** 单位或选项 */
    private String unit;
    /** 最小值（仅数值型有效） */
    private Double minValue;
    /** 最大值（仅数值型有效） */
    private Double maxValue;
    /** 是否必填：0否 1是 */
    private Boolean requiredFlag;

    public static InspectItemDto covert(InspectItem source) {
        if (source == null) return null;
        InspectItemDto target = new InspectItemDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<InspectItemDto> covert(List<InspectItem> sources) {
        List<InspectItemDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (InspectItem source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static <T extends InspectItemDto> InspectItem wrap(T source) {
        if (source == null) return null;
        InspectItem target = new InspectItem();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends InspectItemDto> List<InspectItem> wrap(List<T> sources) {
        List<InspectItem> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
