package com.ourexists.omes.ai.model.inspection;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class InspectRecordDto {

    private String id;

    private String equipName;

    private String equipSelfCode;

    private Date recordTime;

    private Double score;

    private List<InspectRecordItemDto> items;
}
