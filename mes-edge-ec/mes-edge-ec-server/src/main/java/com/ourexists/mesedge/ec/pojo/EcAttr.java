package com.ourexists.mesedge.ec.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import com.ourexists.mesedge.ec.model.EcAttrDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_ec_attr")
public class EcAttr extends MainEntity {

    private String name;

    private String map;

    private String workshopId;

    private Integer sort;

    public static EcAttrDto covert(EcAttr source) {
        if (source == null) {
            return null;
        }
        EcAttrDto target = new EcAttrDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<EcAttrDto> covert(List<EcAttr> sources) {
        List<EcAttrDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (EcAttr source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }


    public static <T extends EcAttrDto> EcAttr wrap(T source) {
        EcAttr target = new EcAttr();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends EcAttrDto> List<EcAttr> wrap(List<T> sources) {
        List<EcAttr> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
