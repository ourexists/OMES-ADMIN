package com.ourexists.mesedge.portal.device.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class EquipCountDto {

    private Integer total = 0;

    private Integer alarm = 0;

    private Integer run = 0;

    private Integer stopped = 0;

    private Integer online = 0;

    private Integer offline = 0;
}
