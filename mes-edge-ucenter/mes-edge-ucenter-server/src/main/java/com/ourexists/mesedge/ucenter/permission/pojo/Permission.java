/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.permission.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.tree.TreeUtil;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import com.ourexists.mesedge.ucenter.permission.PermissionDto;
import com.ourexists.mesedge.ucenter.permission.PermissionTreeNode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

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
@TableName("p_ucenter_permission")
public class Permission extends MainEntity {

    private String code;

    /**
     * 父id
     */
    private String pcode;


    private String ppcode;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限策略
     * 0:启用并显示
     * 1:启用但不显示
     * 2:禁用
     */
    private Integer strategy;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 组件
     */
    private String component;

    /**
     * 跳转网页链接
     */
    private String url;

    /**
     * 菜单排序
     */
    private Double sortNo;

    /**
     * 类型（0:菜单权限  1:按钮权限  2:空描述权限）
     */
    private Integer type;

    /**
     * 是否路缓存页面: 0:不是  1:是（默认值1）
     */
    private boolean keepAlive;

    /**
     * 描述
     */
    private String description;

    /**
     * 外链菜单打开方式
     * 0:内部打开 1:外部打开
     */
    private Integer internalOrExternal;

    private String platform;

    public static Permission warp(PermissionDto source) {
        Permission target = new Permission();
        BeanUtils.copyProperties(source, target);
        if (TreeUtil.ROOT_CODE.equals(source.getPcode())) {
            target.setPcode(null);
        }
        return target;
    }

    public static PermissionTreeNode covert(Permission source) {
        PermissionTreeNode target = new PermissionTreeNode();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<PermissionTreeNode> covert(List<Permission> sources) {
        List<PermissionTreeNode> targets = new ArrayList<>();
        if (!CollectionUtils.isEmpty(sources)) {
            for (Permission source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static PermissionDto covertDto(Permission source) {
        PermissionDto target = new PermissionDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<PermissionDto> covertDto(List<Permission> sources) {
        List<PermissionDto> targets = new ArrayList<>();
        if (!CollectionUtils.isEmpty(sources)) {
            for (Permission source : sources) {
                targets.add(covertDto(source));
            }
        }
        return targets;
    }
}
