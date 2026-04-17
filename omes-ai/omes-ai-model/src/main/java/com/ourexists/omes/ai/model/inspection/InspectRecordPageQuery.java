package com.ourexists.omes.ai.model.inspection;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class InspectRecordPageQuery {

    private Date recordTimeStart;

    private String taskId;

    private String equipName;

    private Integer page;

    private Integer pageSize;
}
