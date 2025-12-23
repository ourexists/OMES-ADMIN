package com.ourexists.mesedge.ec.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class EcAttrBatchDto {

    private String workshopId;

    private List<EcAttrDto> ecAttrs;
}
