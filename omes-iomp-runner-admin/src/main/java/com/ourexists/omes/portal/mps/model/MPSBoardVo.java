/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.mps.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class MPSBoardVo {

    @Schema(description = "待排产")
    private List<MPSVo> waitQue = new ArrayList<>();

    @Schema(description = "待执行队列")
    private List<MPSVo> waitExec = new ArrayList<>();

    @Schema(description = "执行中")
    private List<MPSVo> execing = new ArrayList<>();

    @Schema(description = "已完成")
    private List<MPSVo> complete = new ArrayList<>();
}
