package com.ourexists.mesedge.device.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.mesedge.device.model.EquipConfigDetail;
import com.ourexists.mesedge.device.model.EquipConfigDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName(value = "r_equip_config", autoResultMap = true)
public class EquipConfig {

    @TableId
    private String equipId;

    @TableField(value = "config", typeHandler = JacksonTypeHandler.class)
    private EquipConfigDetail config;

    public static EquipConfigDto covert(EquipConfig source) {
        if (source == null) {
            return null;
        }
        EquipConfigDto target = new EquipConfigDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<EquipConfigDto> covert(List<EquipConfig> sources) {
        List<EquipConfigDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (EquipConfig source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }


    public static <T extends EquipConfigDto> EquipConfig wrap(T source) {
        EquipConfig target = new EquipConfig();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends EquipConfigDto> List<EquipConfig> wrap(List<T> sources) {
        List<EquipConfig> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
