package com.ourexists.omes.device.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.omes.device.model.EquipConfigDetail;
import com.ourexists.omes.device.model.GwBindingDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.JdbcType;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName(value = "r_gw_binding", autoResultMap = true)
public class GwBinding {

    @TableId
    private String equipId;

    private String gwId;

    @TableField(value = "config", typeHandler = JacksonTypeHandler.class, jdbcType = JdbcType.OTHER)
    private EquipConfigDetail config;

    public static GwBindingDto covert(GwBinding source) {
        if (source == null) {
            return null;
        }
        source.getConfig().setGwId(source.getGwId());
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
        source.getConfig().setGwId(source.getGwId());
        GwBinding target = new GwBinding();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends GwBindingDto> List<GwBinding> wrap(List<T> sources) {
        List<GwBinding> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
