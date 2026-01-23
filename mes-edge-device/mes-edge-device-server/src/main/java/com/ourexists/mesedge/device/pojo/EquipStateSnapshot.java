package com.ourexists.mesedge.device.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.mesedge.device.model.EquipStateSnapshotDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_equip_state_snapshot")
public class EquipStateSnapshot extends EraEntity {

    private String sn;

    private Date time;

    private Integer runState;

    private Integer alarmState;

    private Integer onlineState;

    public static <T extends EquipStateSnapshotDto> T covert(EquipStateSnapshot source, Class<T> clazz) {
        if (source == null) {
            return null;
        }
        T target;
        try {
            target = clazz.newInstance();
            BeanUtils.copyProperties(source, target);
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }

        return target;
    }

    public static <T extends EquipStateSnapshotDto> List<T> covert(List<EquipStateSnapshot> sources, Class<T> clazz) {
        List<T> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (EquipStateSnapshot source : sources) {
                targets.add(covert(source, clazz));
            }
        }
        return targets;
    }

    public static <T extends EquipStateSnapshotDto> EquipStateSnapshot wrap(T source) {
        EquipStateSnapshot target = new EquipStateSnapshot();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends EquipStateSnapshotDto> List<EquipStateSnapshot> wrap(List<T> sources) {
        List<EquipStateSnapshot> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
