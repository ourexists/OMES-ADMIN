package com.ourexists.mesedge.report.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class FzDataDto {

    private Integer no;

    private Date rq;

    private Integer line;

    private String pNo;

    private String bh;

    private String oper;

    private String stTime;

    private String endTime;

    private String pf;

}
