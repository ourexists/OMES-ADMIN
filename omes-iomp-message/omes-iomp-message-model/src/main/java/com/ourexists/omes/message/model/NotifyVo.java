package com.ourexists.omes.message.model;

import com.ourexists.omes.message.enums.MessageTypeEnum;
import com.ourexists.omes.message.enums.NotifyStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class NotifyVo extends NotifyDto {

    private String typeDesc;

    private Integer status;

    private String statusDesc;

    private String createdBy;

    private String createdId;

    private Date createdTime;


    public String getStatusDesc() {
        return NotifyStatusEnum.valueOf(this.status).name();
    }


    public String getTypeDesc() {
        return MessageTypeEnum.valueOf(this.type).name();
    }
}
