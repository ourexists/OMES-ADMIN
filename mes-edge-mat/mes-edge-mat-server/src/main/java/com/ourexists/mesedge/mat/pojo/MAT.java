/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.pojo;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import com.ourexists.mesedge.mat.model.MaterialDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_mat")
public class MAT extends MainEntity {

    private String selfCode;

    private String name;

    private String classifyCode;

    @TableLogic
    private Boolean delBit;

    public static MaterialDto covert(MAT source) {
        if (source == null) {
            return null;
        }
        MaterialDto target = new MaterialDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<MaterialDto> covert(List<MAT> sources) {
        List<MaterialDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (MAT source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }


    public static MAT wrap(MaterialDto source) {
        MAT target = new MAT();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<MAT> wrap(List<MaterialDto> sources) {
        List<MAT> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (MaterialDto source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
