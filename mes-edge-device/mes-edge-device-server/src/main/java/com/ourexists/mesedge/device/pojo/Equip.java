package com.ourexists.mesedge.device.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.mesedge.device.model.EquipDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_equip")
public class Equip extends EraEntity {

    private String name;

    private String selfCode;

    private Integer type;

    private String workshopCode;

    private BigDecimal lng;

    private BigDecimal lat;

    private String address;

    public static EquipDto covert(Equip source) {
        if (source == null) {
            return null;
        }
        EquipDto target = new EquipDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<EquipDto> covert(List<Equip> sources) {
        List<EquipDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (Equip source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static <T extends EquipDto> Equip wrap(T source) {
        Equip target = new Equip();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends EquipDto> List<Equip> wrap(List<T> sources) {
        List<Equip> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
