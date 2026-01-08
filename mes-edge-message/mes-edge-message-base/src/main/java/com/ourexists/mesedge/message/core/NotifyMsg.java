package com.ourexists.mesedge.message.core;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotifyMsg {

    private String id;

    private String title;

    private String context;

    private Integer type;

    private String platform;

    private String tenantId;

    private String source;

    private String sourceId;
}
