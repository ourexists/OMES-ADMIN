package com.ourexists.mesedge.report.model;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Schema
@Accessors(chain = true)
public class WinCCDosingPageQuery extends PageQuery {

    private Date startDate;

    private Date endDate;
}
