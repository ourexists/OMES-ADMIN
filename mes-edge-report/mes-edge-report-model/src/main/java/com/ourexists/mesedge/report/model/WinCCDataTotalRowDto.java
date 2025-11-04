package com.ourexists.mesedge.report.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Schema
@Accessors(chain = true)
public class WinCCDataTotalRowDto {

    private WinCCDatalistDto avg;

    private WinCCDatalistDto total;

    private WinCCDatalistDto max;

    private WinCCDatalistDto min;
}
