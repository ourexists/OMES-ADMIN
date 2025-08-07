/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.pojo;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import com.ourexists.mesedge.mps.model.MPSDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 配方表
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_mps")
public class MPS extends MainEntity {

    /**
     * 配方编号
     */
    private String moCode;


    /**
     * 生产执行序列号
     */
    private Integer sequence;

    /**
     * 在=0时，配料系统的参数属于默认的。比如：油水算入比例测算等。否则，取决于配料系统的设置。
     */
    private String line;

    private Date execTime;

    private Integer num;

    private BigDecimal weight;

    /**
     * 批次
     */
    private Integer batch;

    private Integer status = 0;

    private BigDecimal priority;

    @TableLogic
    private Boolean delBit;

    public static MPSDto covert(MPS source) {
        if (source == null) {
            return null;
        }
        MPSDto target = new MPSDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<MPSDto> covert(List<MPS> sources) {
        List<MPSDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static MPS wrap(MPSDto source) {
        MPS target = new MPS();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<MPS> wrap(List<MPSDto> sources) {
        List<MPS> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }
}
