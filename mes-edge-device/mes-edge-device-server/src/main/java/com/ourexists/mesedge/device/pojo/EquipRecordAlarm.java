package com.ourexists.mesedge.device.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.mesedge.device.model.EquipRecordAlarmDto;
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
@TableName("t_equip_record_alarm")
public class EquipRecordAlarm extends EraEntity {

    private String sn;

    private Date startTime;

    private Date endTime;

    private Integer state;

    private String reason;

    public static <T extends EquipRecordAlarmDto> T covert(EquipRecordAlarm source, Class<T> clazz) {
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

    public static <T extends EquipRecordAlarmDto> List<T> covert(List<EquipRecordAlarm> sources, Class<T> clazz) {
        List<T> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (EquipRecordAlarm source : sources) {
                targets.add(covert(source, clazz));
            }
        }
        return targets;
    }

    public static <T extends EquipRecordAlarmDto> EquipRecordAlarm wrap(T source) {
        EquipRecordAlarm target = new EquipRecordAlarm();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends EquipRecordAlarmDto> List<EquipRecordAlarm> wrap(List<T> sources) {
        List<EquipRecordAlarm> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
