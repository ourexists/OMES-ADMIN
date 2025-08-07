/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import com.ourexists.mesedge.mat.model.MaterialClassifyDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_mc")
public class MC extends MainEntity {

    private String selfCode;

    private String code;

    private String pcode;

    private String name;

    public static MaterialClassifyDto covert(MC source) {
        if (source == null) {
            return null;
        }
        MaterialClassifyDto target = new MaterialClassifyDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<MaterialClassifyDto> covert(List<MC> sources) {
        List<MaterialClassifyDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (MC source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }


    public static MC wrap(MaterialClassifyDto source) {
        MC target = new MC();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<MC> wrap(List<MaterialClassifyDto> sources) {
        List<MC> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (MaterialClassifyDto source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
