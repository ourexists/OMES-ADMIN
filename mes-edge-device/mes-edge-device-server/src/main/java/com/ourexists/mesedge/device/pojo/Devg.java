/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.pojo;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import com.ourexists.mesedge.device.model.DevgDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_dg")
public class Devg extends MainEntity {

    private String selfCode;

    private String name;

    public static DevgDto covert(Devg source) {
        if (source == null) {
            return null;
        }
        DevgDto target = new DevgDto();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static List<DevgDto> covert(List<Devg> sources) {
        List<DevgDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (Devg source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }


    public static Devg wrap(DevgDto source) {
        Devg target = new Devg();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static List<Devg> wrap(List<DevgDto> sources) {
        List<Devg> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (DevgDto source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
