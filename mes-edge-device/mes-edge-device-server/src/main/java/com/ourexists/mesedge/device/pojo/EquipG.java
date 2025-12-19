/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 配方分类
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_equip_g")
public class EquipG extends MainEntity {

    private String selfCode;

    //级联编号
    private String code;

    private String pcode;


    private String name;

    private String description;
}
