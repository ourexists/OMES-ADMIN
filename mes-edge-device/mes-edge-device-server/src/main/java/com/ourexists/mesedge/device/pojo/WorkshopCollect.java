package com.ourexists.mesedge.device.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.mesedge.device.model.WorkshopCollectDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
@TableName(value = "t_workshop_collect", autoResultMap = true)
public class WorkshopCollect extends EraEntity {

    private String workshopId;

    @TableField(value = "data", typeHandler = JacksonTypeHandler.class)
    private Map<String, String> data;

    private Date time;

    public static WorkshopCollectDto covert(WorkshopCollect source) {
        if (source == null) {
            return null;
        }
        WorkshopCollectDto target = new WorkshopCollectDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<WorkshopCollectDto> covert(List<WorkshopCollect> sources) {
        List<WorkshopCollectDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (WorkshopCollect source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }


    public static <T extends WorkshopCollectDto> WorkshopCollect wrap(T source) {
        WorkshopCollect target = new WorkshopCollect();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends WorkshopCollectDto> List<WorkshopCollect> wrap(List<T> sources) {
        List<WorkshopCollect> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
