package com.ourexists.mesedge.device.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.mesedge.device.model.WorkshopConfigCollectDetail;
import com.ourexists.mesedge.device.model.WorkshopConfigCollectDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Accessors(chain = true)
@TableName(value = "r_workshop_config_collect", autoResultMap = true)
public class WorkshopConfigCollect {

    @TableId
    private String workshopId;

    @TableField(value = "config", typeHandler = JacksonTypeHandler.class)
    private WorkshopConfigCollectDetail config;

    public static WorkshopConfigCollectDto covert(WorkshopConfigCollect source) {
        if (source == null) {
            return null;
        }
        WorkshopConfigCollectDto target = new WorkshopConfigCollectDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<WorkshopConfigCollectDto> covert(List<WorkshopConfigCollect> sources) {
        List<WorkshopConfigCollectDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (WorkshopConfigCollect source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }


    public static <T extends WorkshopConfigCollectDto> WorkshopConfigCollect wrap(T source) {
        WorkshopConfigCollect target = new WorkshopConfigCollect();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends WorkshopConfigCollectDto> List<WorkshopConfigCollect> wrap(List<T> sources) {
        List<WorkshopConfigCollect> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
