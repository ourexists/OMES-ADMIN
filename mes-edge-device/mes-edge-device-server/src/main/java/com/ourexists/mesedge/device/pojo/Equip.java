package com.ourexists.mesedge.device.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_equip")
public class Equip extends MainEntity {

    private String name;

    private String selfCode;

    private Integer type;

    private String runMap;

    private String alarmMap;

    private String gId;

}
