package com.ourexists.omes.device.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class WorkshopAssignDto {

    private String assignId;

    private String workshopCode;
}
