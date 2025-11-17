package com.ourexists.mesedge.portal.task;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class WinccDeviceCount {

    private Integer run = 0;

    private Integer stop = 0;

    private Integer alarm = 0;

    private Map<String, Integer> device;

}
