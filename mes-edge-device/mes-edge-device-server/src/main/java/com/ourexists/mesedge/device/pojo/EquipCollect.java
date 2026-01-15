package com.ourexists.mesedge.device.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.mesedge.device.model.EquipCollectDto;
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
@TableName(value = "t_equip_collect", autoResultMap = true)
public class EquipCollect extends EraEntity {

    private String sn;

    @TableField(value = "data", typeHandler = JacksonTypeHandler.class)
    private Map<String, String> data;

    private Date time;

    public static EquipCollectDto covert(EquipCollect source) {
        if (source == null) {
            return null;
        }
        EquipCollectDto target = new EquipCollectDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<EquipCollectDto> covert(List<EquipCollect> sources) {
        List<EquipCollectDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (EquipCollect source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }


    public static <T extends EquipCollectDto> EquipCollect wrap(T source) {
        EquipCollect target = new EquipCollect();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends EquipCollectDto> List<EquipCollect> wrap(List<T> sources) {
        List<EquipCollect> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
