package com.ourexists.omes.device.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.omes.device.model.GwBindingDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("r_gw_binding")
public class GwBinding {

    @TableId
    private String equipId;

    private String gwId;

    public static GwBindingDto covert(GwBinding source) {
        if (source == null) {
            return null;
        }
        GwBindingDto target = new GwBindingDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<GwBindingDto> covert(List<GwBinding> sources) {
        List<GwBindingDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (GwBinding source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static <T extends GwBindingDto> GwBinding wrap(T source) {
        if (source == null) {
            return null;
        }
        GwBinding target = new GwBinding();
        target.setEquipId(source.getEquipId());
        target.setGwId(source.getGwId());
        return target;
    }
}
