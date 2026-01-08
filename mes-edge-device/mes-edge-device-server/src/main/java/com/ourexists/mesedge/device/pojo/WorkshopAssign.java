package com.ourexists.mesedge.device.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.mesedge.device.model.WorkshopAssignDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("r_workshop_assign")
public class WorkshopAssign {

    private String assignId;

    private String workshopCode;

    public static WorkshopAssignDto covert(WorkshopAssign source) {
        if (source == null) {
            return null;
        }
        WorkshopAssignDto target = new WorkshopAssignDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<WorkshopAssignDto> covert(List<WorkshopAssign> sources) {
        List<WorkshopAssignDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (WorkshopAssign source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static <T extends WorkshopAssignDto> WorkshopAssign wrap(T source) {
        WorkshopAssign target = new WorkshopAssign();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends WorkshopAssignDto> List<WorkshopAssign> wrap(List<T> sources) {
        List<WorkshopAssign> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
