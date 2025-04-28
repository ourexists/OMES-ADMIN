/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.depart.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.tree.TreeUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.mesedge.ucenter.depart.DepartDto;
import com.ourexists.mesedge.ucenter.depart.DepartTreeNode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:13
 * @since 1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_ucenter_depart")
public class Depart extends EraEntity {

    /**
     * 部门名
     */
    private String name;

    /**
     * 部门编号
     */
    private String code;

    /**
     * 部门归属的上级部门的code
     */
    private String pcode;

    /**
     * 部门归属的所有上级部门的code.
     * ;分隔
     */
    private String ppcode;

    public static Depart warp(DepartDto source) {
        Depart target = new Depart();
        BeanUtils.copyProperties(source, target);
        if (TreeUtil.ROOT_CODE.equals(source.getPcode())) {
            target.setPcode(null);
        }
        return target;
    }

    public static DepartTreeNode covert(Depart source) {
        DepartTreeNode target = new DepartTreeNode();
        BeanUtils.copyProperties(source, target);
        if (StringUtils.isEmpty(source.getPcode())) {
            target.setPcode(TreeUtil.ROOT_CODE);
        }
        return target;
    }

    public static List<DepartTreeNode> covert(List<Depart> sources) {
        List<DepartTreeNode> targets = new ArrayList<>();
        if (!CollectionUtils.isEmpty(sources)) {
            for (Depart source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }
}
