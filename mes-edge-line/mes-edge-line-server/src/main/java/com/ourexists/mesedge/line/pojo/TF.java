/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.line.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.mesedge.line.model.TFDto;
import com.ourexists.mesedge.line.model.TFVo;
import io.swagger.v3.oas.annotations.media.Schema;
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
@TableName("t_tf")
public class TF extends EraEntity {

    private String selfCode;

    private String name;

    private Integer mapDb;

    private String mapOffset;

    private String property;

    private Long duration;

    private Integer type;

    private String pre;

    private String nex;

    private String lineId;

    private BigDecimal priority;

    @Schema(description = "设定温度")
    private Double temperature;

    public static TFVo covert(TF source) {
        if (source == null) {
            return null;
        }
        TFVo target = new TFVo();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<TFVo> covert(List<TF> sources) {
        List<TFVo> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (TF source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }


    public static TF wrap(TFDto source) {
        TF target = new TF();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<TF> wrap(List<TFDto> sources) {
        List<TF> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (TFDto source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }

    public static List<TF> wrap(List<TFDto> sources, String lineId) {
        List<TF> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (TFDto source : sources) {
                TF tf = wrap(source);
                tf.setLineId(lineId);
                targets.add(tf);
            }
        }
        return targets;
    }
}
