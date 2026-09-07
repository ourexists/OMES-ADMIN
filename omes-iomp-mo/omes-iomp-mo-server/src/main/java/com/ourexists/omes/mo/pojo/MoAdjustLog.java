/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.mo.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.omes.mo.model.MoAdjustLogDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_mo_adjust_log")
public class MoAdjustLog extends EraEntity {

    private String requestId;

    private String moCode;

    private String adjustType;

    private String source;

    private String beforeJson;

    private String afterJson;

    private String affectMpsIds;

    private Integer status;

    private String errMsg;

    private String operator;

    /** 业务创建时间（与 MainEntity.createdTime 并存，便于审计查询） */
    private Date createTime;

    public static MoAdjustLogDto covert(MoAdjustLog source) {
        if (source == null) {
            return null;
        }
        MoAdjustLogDto target = new MoAdjustLogDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<MoAdjustLogDto> covert(List<MoAdjustLog> sources) {
        List<MoAdjustLogDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (MoAdjustLog source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static MoAdjustLog wrap(MoAdjustLogDto source) {
        MoAdjustLog target = new MoAdjustLog();
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
