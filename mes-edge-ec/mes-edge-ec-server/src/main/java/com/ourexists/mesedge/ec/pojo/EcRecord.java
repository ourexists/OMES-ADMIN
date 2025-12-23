package com.ourexists.mesedge.ec.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.mesedge.ec.model.EcRecordDto;
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
@TableName("r_ec_record")
public class EcRecord {

    private String attrId;

    private String attrVal;

    private Date time;

    private String recordId;

    public static EcRecordDto covert(EcRecord source) {
        if (source == null) {
            return null;
        }
        EcRecordDto target = new EcRecordDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<EcRecordDto> covert(List<EcRecord> sources) {
        List<EcRecordDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (EcRecord source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }


    public static <T extends EcRecordDto> EcRecord wrap(T source) {
        EcRecord target = new EcRecord();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends EcRecordDto> List<EcRecord> wrap(List<T> sources) {
        List<EcRecord> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
