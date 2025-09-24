package com.ourexists.mesedge.report.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Schema
@Accessors(chain = true)
public class MatCountQuery {

    private Date startDate;

    private Date endDate;

    private String matName;

    private String pfName;

    private String bh;

    private String line;

}
