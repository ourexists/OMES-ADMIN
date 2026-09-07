package com.ourexists.omes.device.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import com.ourexists.omes.device.model.WorkshopConfigMeta2dDto;
import com.ourexists.omes.device.model.WorkshopMeta2dConfigDetail;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName(value = "r_workshop_config_meta2d", autoResultMap = true)
public class WorkshopConfigMeta2d extends MainEntity {

    private String workshopId;

    @TableField(value = "meta2d_config", typeHandler = JacksonTypeHandler.class)
    private WorkshopMeta2dConfigDetail meta2dConfig;

    public static WorkshopConfigMeta2dDto covert(WorkshopConfigMeta2d source) {
        if (source == null) {
            return null;
        }
        WorkshopConfigMeta2dDto target = new WorkshopConfigMeta2dDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<WorkshopConfigMeta2dDto> covert(List<WorkshopConfigMeta2d> sources) {
        List<WorkshopConfigMeta2dDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (WorkshopConfigMeta2d source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static <T extends WorkshopConfigMeta2dDto> WorkshopConfigMeta2d wrap(T source) {
        WorkshopConfigMeta2d target = new WorkshopConfigMeta2d();
        BeanUtils.copyProperties(source, target);
        return target;
    }
}

