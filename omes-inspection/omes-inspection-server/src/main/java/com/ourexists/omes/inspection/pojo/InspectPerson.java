/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.omes.inspection.model.InspectPersonDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 巡检人员
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_inspect_person")
public class InspectPerson extends EraEntity {

    /** 姓名 */
    private String name;
    /** 关联账户ID，为空表示未关联 */
    private String accountId;
    /** 关联账户名称（冗余展示） */
    private String accountName;
    /** 手机号 */
    private String mobile;
    /** 工号 */
    private String jobNumber;
    /** 备注 */
    private String remark;

    public static InspectPersonDto covert(InspectPerson source) {
        if (source == null) return null;
        InspectPersonDto target = new InspectPersonDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<InspectPersonDto> covert(List<InspectPerson> sources) {
        List<InspectPersonDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (InspectPerson source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static <T extends InspectPersonDto> InspectPerson wrap(T source) {
        if (source == null) return null;
        InspectPerson target = new InspectPerson();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends InspectPersonDto> List<InspectPerson> wrap(List<T> sources) {
        List<InspectPerson> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
