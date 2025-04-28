/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.depart;


import com.ourexists.era.framework.core.utils.tree.TreeNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author pengcheng
 * @date 2022/4/11 15:48
 * @since 1.0.0
 */
@Getter
@Setter
@Schema
public class DepartTreeNode extends TreeNode<DepartTreeNode> {

    private String id;

    @Schema(description = "用户组名")
    private String name;

    private String tenantId;
}
