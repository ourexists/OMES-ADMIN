package com.ourexists.omes.ai.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AiInspectionReportResponse {

    private String report;

    private Integer recordCount;

    private Integer abnormalCount;

    private String modelUsed;

    private List<String> retrievedKnowledge;
}
