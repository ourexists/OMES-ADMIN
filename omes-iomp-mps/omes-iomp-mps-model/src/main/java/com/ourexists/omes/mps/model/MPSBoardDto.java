/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.mps.model;

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
public class MPSBoardDto {

    @Schema(description = "待排产")
    private List<MPSDto> waitQue = new ArrayList<>();

    @Schema(description = "待执行队列")
    private List<MPSDto> waitExec = new ArrayList<>();

    @Schema(description = "执行中")
    private List<MPSDto> execing = new ArrayList<>();

    @Schema(description = "已完成")
    private List<MPSDto> complete = new ArrayList<>();
}
