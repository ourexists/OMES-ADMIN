package com.ourexists.mesedge.device.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.mesedge.device.model.EquipAttrDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_equip_attr")
public class EquipAttr extends EraEntity {

    private String equipId;

    private String name;

    private String map;

    private Integer sort;

    public static EquipAttrDto covert(EquipAttr source) {
        if (source == null) {
            return null;
        }
        EquipAttrDto target = new EquipAttrDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<EquipAttrDto> covert(List<EquipAttr> sources) {
        List<EquipAttrDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (EquipAttr source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }


    public static <T extends EquipAttrDto> EquipAttr wrap(T source) {
        EquipAttr target = new EquipAttr();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends EquipAttrDto> List<EquipAttr> wrap(List<T> sources) {
        List<EquipAttr> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
