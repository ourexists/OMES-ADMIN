/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.pojo;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import com.ourexists.mesedge.mps.model.MPSDetailDto;
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
@TableName("r_mps_d")
public class MPSDetail extends MainEntity {

    private String matName;

    private String matCode;

    private String matId;

    /**
     * 物料量
     */
    private BigDecimal matNum;

    private Integer priority;

    private String moCode;

    /**
     * 对应的配方编号
     */
    private String mid;

    /**
     * 设备编号
     */
    private String devNo;


    /**
     * 组份性质：0=主料，1=预混料（添加剂）,2=回机料,3=油,4=水
     */
    private Integer attribute;

    @TableLogic
    private Boolean delBit;

    public static MPSDetailDto covert(MPSDetail source) {
        if (source == null) {
            return null;
        }
        MPSDetailDto target = new MPSDetailDto();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static List<MPSDetailDto> covert(List<MPSDetail> sources) {
        List<MPSDetailDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static MPSDetail wrap(MPSDetailDto source) {
        MPSDetail target = new MPSDetail();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static List<MPSDetail> wrap(List<MPSDetailDto> sources) {
        List<MPSDetail> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }

    public static List<MPSDetail> wrap(List<MPSDetailDto> sources, String mid, String moCode) {
        List<MPSDetail> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> {
                MPSDetail mpsDetail = wrap(source);
                mpsDetail.setMid(mid);
                mpsDetail.setMoCode(moCode);
                targets.add(mpsDetail);
            });
        }
        return targets;
    }
}
