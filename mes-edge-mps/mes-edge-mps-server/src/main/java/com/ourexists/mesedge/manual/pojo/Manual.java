/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.manual.pojo;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.mesedge.mps.model.QADto;
import com.ourexists.mesedge.mps.model.QAVo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_qa")
public class Manual extends EraEntity {

    private String mpsId;

    private String mpsTfId;

    private String msg;

    private Integer result;

    private BigDecimal pass;

    public static QAVo covert(Manual source) {
        if (source == null) {
            return null;
        }
        QAVo target = new QAVo();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static List<QAVo> covert(List<Manual> sources) {
        List<QAVo> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static <T extends QADto> Manual wrap(T source) {
        Manual target = new Manual();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static <T extends QADto> List<Manual> wrap(List<T> sources) {
        List<Manual> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }
}
