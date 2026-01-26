package com.ourexists.mesedge.portal.device.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ScadaUrlDto {

    private String url;

    private Integer Interval;
}
