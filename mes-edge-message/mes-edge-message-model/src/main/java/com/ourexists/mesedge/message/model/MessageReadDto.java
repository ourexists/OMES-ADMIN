package com.ourexists.mesedge.message.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class MessageReadDto {

    private String messageId;

    private String accId;

    private Date time;

    private Boolean isRead = false;
}
