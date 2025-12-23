package com.ourexists.mesedge.device.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.mesedge.device.model.EquipRunRecordDto;
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
@TableName("t_equip_run_record")
public class EquipRunRecord extends EraEntity {

    private String sn;

    private Date runStartTime;

    private Date runEndTime;

    private BigDecimal powerStart;

    private BigDecimal powerEnd;

    public static EquipRunRecordDto covert(EquipRunRecord source) {
        if (source == null) {
            return null;
        }
        EquipRunRecordDto target = new EquipRunRecordDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<EquipRunRecordDto> covert(List<EquipRunRecord> sources) {
        List<EquipRunRecordDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (EquipRunRecord source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static <T extends EquipRunRecordDto> EquipRunRecord wrap(T source) {
        EquipRunRecord target = new EquipRunRecord();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends EquipRunRecordDto> List<EquipRunRecord> wrap(List<T> sources) {
        List<EquipRunRecord> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
