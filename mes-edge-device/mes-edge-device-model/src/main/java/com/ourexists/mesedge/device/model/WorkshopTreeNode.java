package com.ourexists.mesedge.device.model;

import com.ourexists.era.framework.core.utils.tree.TreeNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class WorkshopTreeNode extends TreeNode<WorkshopTreeNode> {

    private String id;

    private String name;

    @Schema(description = "编号")
    private String selfCode;
}
