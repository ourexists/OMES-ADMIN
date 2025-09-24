package com.ourexists.mesedge.report.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class LmRecordDto {

    private Integer no;

    private Date rq;

    private String pf;

    private String bh;

    private String line;

    private String pNo;

    private String oper;

    private String tank;

    private String lm;

    private Float ll;

    private Float sj;

    private Float wc;

    private Float jd;

    private Integer fzId;

}
