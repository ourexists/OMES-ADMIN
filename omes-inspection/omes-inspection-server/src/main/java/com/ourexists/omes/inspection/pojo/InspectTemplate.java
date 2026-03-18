/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.omes.inspection.model.InspectTemplateDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 巡检模板（如：水泵巡检模板）
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_inspect_template")
public class InspectTemplate extends EraEntity {

    /** 模板名称 */
    private String name;
    /** 备注 */
    private String remark;

    public static InspectTemplateDto covert(InspectTemplate source) {
        if (source == null) return null;
        InspectTemplateDto target = new InspectTemplateDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<InspectTemplateDto> covert(List<InspectTemplate> sources) {
        List<InspectTemplateDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (InspectTemplate source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static <T extends InspectTemplateDto> InspectTemplate wrap(T source) {
        if (source == null) return null;
        InspectTemplate target = new InspectTemplate();
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
