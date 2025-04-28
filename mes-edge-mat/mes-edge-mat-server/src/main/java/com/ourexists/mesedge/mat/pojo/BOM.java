/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.pojo;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import com.ourexists.mesedge.mat.model.BOMDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * 配方表
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_bom")
public class BOM extends MainEntity {

    /**
     * 配方名称
     */
    private String name;

    /**
     * 配方编号
     */
    private String selfCode;

    /**
     * 所属分类编号
     */
    private String classifyCode;

    /**
     * 是否为非标准配方
     */
    private Integer type;

    /**
     * 在=0时，配料系统的参数属于默认的。比如：油水算入比例测算等。否则，取决于配料系统的设置。
     */
    private Integer line;


    @TableLogic
    private Boolean delBit;

    public static BOMDto covert(BOM source) {
        if (source == null) {
            return null;
        }
        BOMDto target = new BOMDto();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static List<BOMDto> covert(List<BOM> sources) {
        List<BOMDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static BOM wrap(BOMDto source) {
        BOM target = new BOM();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static List<BOM> wrap(List<BOMDto> sources) {
        List<BOM> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }
}
