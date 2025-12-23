package com.ourexists.mesedge.ec.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class EcRecordDto {

    private String attrId;

    private String attrVal;

    private Date time;

    private String recordId;

}
