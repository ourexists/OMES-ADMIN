package com.ourexists.mesedge.message.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
@TableName("r_message_read")
public class MessageRead {

    private String messageId;

    private String accId;

    private Date time;
}
