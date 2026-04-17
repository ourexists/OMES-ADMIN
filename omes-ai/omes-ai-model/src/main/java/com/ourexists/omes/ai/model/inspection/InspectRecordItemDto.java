package com.ourexists.omes.ai.model.inspection;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InspectRecordItemDto {

    private String itemName;

    private Integer result;

    private String resultDesc;

    private String remark;
}
