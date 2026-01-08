package com.ourexists.mesedge.message.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class MessageDto {

    protected String id;

    protected String title;

    protected String context;

    protected Integer type;

    protected String platform;

    protected String notifyId;

    protected String source;

    protected String sourceId;

    protected List<String> sendAccounts;
}
