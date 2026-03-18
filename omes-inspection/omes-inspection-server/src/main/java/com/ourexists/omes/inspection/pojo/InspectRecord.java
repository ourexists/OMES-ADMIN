/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.omes.inspection.model.InspectRecordDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 巡检记录
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_inspect_record")
public class InspectRecord extends EraEntity {

    private String taskId;
    private String equipId;
    private String equipName;
    /** 巡检分值 */
    private Integer score;
    private Date recordTime;

    public static InspectRecordDto covert(InspectRecord source) {
        if (source == null) return null;
        InspectRecordDto target = new InspectRecordDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<InspectRecordDto> covert(List<InspectRecord> sources) {
        List<InspectRecordDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (InspectRecord source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static <T extends InspectRecordDto> InspectRecord wrap(T source) {
        if (source == null) return null;
        InspectRecord target = new InspectRecord();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends InspectRecordDto> List<InspectRecord> wrap(List<T> sources) {
        List<InspectRecord> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
