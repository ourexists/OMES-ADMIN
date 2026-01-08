package com.ourexists.mesedge.device.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class WorkshopAssignBatchDto {

    private String assignId;

    private List<String> workshopCodes;
}
