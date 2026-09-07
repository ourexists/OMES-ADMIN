package com.ourexists.omes.device.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@TableName("r_dg_equip")
public class DgEquip {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String dgId;

    private String equipId;
}
