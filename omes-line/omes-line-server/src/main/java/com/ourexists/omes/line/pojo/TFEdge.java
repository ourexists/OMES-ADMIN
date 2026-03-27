/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.line.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.omes.line.model.TFEdgeDto;
import com.ourexists.omes.line.model.TFEdgeVo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_tf_edge")
public class TFEdge extends EraEntity {

    private String lineId;

    private String fromTfId;

    private String toTfId;

    public static TFEdgeVo covert(TFEdge source) {
        if (source == null) {
            return null;
        }
        TFEdgeVo target = new TFEdgeVo();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<TFEdgeVo> covert(List<TFEdge> sources) {
        List<TFEdgeVo> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (TFEdge source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static TFEdge wrap(TFEdgeDto source) {
        TFEdge target = new TFEdge();
        if (source == null) {
            return target;
        }
        BeanUtils.copyProperties(source, target);
        return target;
    }
}

