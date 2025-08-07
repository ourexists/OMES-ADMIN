/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import com.ourexists.mesedge.mat.model.BOMCDto;
import com.ourexists.mesedge.mat.model.BOMTreeNode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 配方分类
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_bom_c")
public class BOMC extends MainEntity {

    private String selfCode;

    //级联编号
    private String code;

    private String pcode;


    private String name;

    private String description;

    public static BOMTreeNode covert(BOMC source) {
        if (source == null) {
            return null;
        }
        BOMTreeNode target = new BOMTreeNode();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<BOMTreeNode> covert(List<BOMC> sources) {
        List<BOMTreeNode> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static BOMC wrap(BOMCDto source) {
        BOMC target = new BOMC();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<BOMC> wrap(List<BOMCDto> sources) {
        List<BOMC> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }
}
