/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.mps.model;

import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.mesedge.line.model.LineVo;
import com.ourexists.mesedge.mo.model.MODto;
import com.ourexists.mesedge.mps.model.MPSDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class MPSVo extends MPSDto {

    @Schema(description = "关联的订单")
    private MODto moDto;

    @Schema(description = "关联的产线")
    private LineVo lineVo;

    public static MPSVo wrap(MPSDto dto) {
        if (dto == null) {
            return null;
        }
        MPSVo mpsVo = new MPSVo();
        BeanUtils.copyProperties(dto, mpsVo);
        return mpsVo;
    }

    public static List<MPSVo> wrap(List<MPSDto> sources) {
        List<MPSVo> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }
}
