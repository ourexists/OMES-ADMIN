package com.ourexists.mesedge.report.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Schema
@Accessors(chain = true)
public class WinCCDatalistResDto {

    private List<WinCCDatalistDto> results;

    private WinCCDataTotalRowDto totalRow;


}
