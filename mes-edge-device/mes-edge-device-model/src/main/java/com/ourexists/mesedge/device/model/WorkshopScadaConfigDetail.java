package com.ourexists.mesedge.device.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Getter
@Setter
@Accessors(chain = true)
public class WorkshopScadaConfigDetail {

    private String server;

    private String privateKey;

    private String privateSecret;

    private String mapCode;

    private Integer interval;

}
