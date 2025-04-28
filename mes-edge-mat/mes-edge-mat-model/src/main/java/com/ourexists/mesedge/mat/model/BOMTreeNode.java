/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.model;

import com.ourexists.era.framework.core.utils.tree.TreeNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class BOMTreeNode extends TreeNode<BOMTreeNode> {

    private String id;

    private String name;

    private String selfCode;

    private String description;
}
