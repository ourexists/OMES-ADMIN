package com.ourexists.mesedge.device.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.mesedge.device.model.EquipRecordRunDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_equip_record_run")
public class EquipRecordRun extends EraEntity {

    private String sn;

    private Date startTime;

    private Date endTime;

    private Integer state;

    private BigDecimal powerStart;

    private BigDecimal powerEnd;

    public static <T extends EquipRecordRunDto> T covert(EquipRecordRun source, Class<T> clazz) {
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

    public static <T extends EquipRecordRunDto> List<T> covert(List<EquipRecordRun> sources, Class<T> clazz) {
        List<T> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (EquipRecordRun source : sources) {
                targets.add(covert(source, clazz));
            }
        }
        return targets;
    }

    public static <T extends EquipRecordRunDto> EquipRecordRun wrap(T source) {
        EquipRecordRun target = new EquipRecordRun();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends EquipRecordRunDto> List<EquipRecordRun> wrap(List<T> sources) {
        List<EquipRecordRun> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
