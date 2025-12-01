/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.permission;

import com.ourexists.era.framework.core.utils.tree.TreeNode;
import com.ourexists.mesedge.ucenter.enums.PermissionStrategyEnum;
import com.ourexists.mesedge.ucenter.enums.PermissionTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author pengcheng
 * @date 2022/4/12 18:45
 * @since 1.0.0
 */
@Getter
@Setter
@Schema
public class PermissionTreeNode extends TreeNode<PermissionTreeNode> {

    private String id;

    @Schema(description = "权限名称")
    private String name;

    @Schema(description = "i18n")
    private String i18n;

    @Schema(description = "权限策略", allowableValues = "0:启用并显示, 1:启用但不显示, 2:禁用")
    private Integer strategy;

    @Schema(description = "权限策略")
    private PermissionStrategyEnum strategyEnum;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "组件")
    private String component;

    @Schema(description = "跳转网页链接")
    private String url;

    @Schema(description = "菜单排序")
    private Double sortNo;

    @Schema(description = "类型", allowableValues = "0:菜单权限,1:按钮权限,2:其它")
    private Integer type;

    @Schema(description = "类型")
    private PermissionTypeEnum typeEnum;


    @Schema(description = "是否路缓存页面")
    private boolean keepAlive;

    @Schema(description = "描述")
    private String description;

//    @Schema(description =  "外链菜单打开方式", allowableValues = "0:内部打开,1:外部打开")
//    private Integer internalOrExternal;

    @Schema(description = "所属平台")
    private String platform;


    public PermissionStrategyEnum getStrategyEnum() {
        return PermissionStrategyEnum.valueof(this.strategy);
    }

    public PermissionTypeEnum getTypeEnum() {
        return PermissionTypeEnum.valueof(this.type);
    }

}
