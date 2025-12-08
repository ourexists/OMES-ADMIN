package com.ourexists.mesedge.message.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class MessageDto {

    protected String id;

    protected String title;

    protected String context;

    protected Integer type;

    protected String platform;

    protected String sourceId;
}
