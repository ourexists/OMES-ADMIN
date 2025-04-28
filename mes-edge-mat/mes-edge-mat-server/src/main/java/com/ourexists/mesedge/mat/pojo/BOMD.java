/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.pojo;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import com.ourexists.mesedge.mat.model.BOMDDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 成分
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("r_bom_d")
public class BOMD extends MainEntity {

    private String matId;

    private String matName;

    private String matCode;

    private BigDecimal matScale;

    /**
     * 对应的配方编号
     */
    private String mcode;

    /**
     * 设备编号
     */
    private String devNo;


    /**
     * 组份性质：0=主料，1=预混料（添加剂）,2=回机料,3=油,4=水
     */
    private Integer attribute;

    public static BOMDDto covert(BOMD source) {
        if (source == null) {
            return null;
        }
        BOMDDto target = new BOMDDto();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static List<BOMDDto> covert(List<BOMD> sources) {
        List<BOMDDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static BOMD wrap(BOMDDto source) {
        BOMD target = new BOMD();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static List<BOMD> wrap(List<BOMDDto> sources) {
        List<BOMD> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }

    public static List<BOMD> wrap(List<BOMDDto> sources, String mcode) {
        List<BOMD> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> {
                source.setMcode(mcode);
                targets.add(wrap(source));
            });
        }
        return targets;
    }

}
