package com.ourexists.mesedge.message.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.logging.log4j.message.Message;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class MessageVo extends MessageDto {

    private Integer readStatus = 0;

    private String createdBy;

    private String createdId;

    private Date createdTime;

    private Date readTime;

}
