package com.ourexists.mesedge.device.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import com.ourexists.mesedge.device.model.WorkshopConfigScadaDto;
import com.ourexists.mesedge.device.model.WorkshopScadaConfigDetail;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Accessors(chain = true)
@TableName(value = "r_workshop_config_scada", autoResultMap = true)
public class WorkshopConfigScada extends MainEntity {

    private String workshopId;

    @TableField(value = "scada_config", typeHandler = JacksonTypeHandler.class)
    private WorkshopScadaConfigDetail scadaConfig;

    public static WorkshopConfigScadaDto covert(WorkshopConfigScada source) {
        if (source == null) {
            return null;
        }
        WorkshopConfigScadaDto target = new WorkshopConfigScadaDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<WorkshopConfigScadaDto> covert(List<WorkshopConfigScada> sources) {
        List<WorkshopConfigScadaDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (WorkshopConfigScada source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }


    public static <T extends WorkshopConfigScadaDto> WorkshopConfigScada wrap(T source) {
        WorkshopConfigScada target = new WorkshopConfigScada();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends WorkshopConfigScadaDto> List<WorkshopConfigScada> wrap(List<T> sources) {
        List<WorkshopConfigScada> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
