package com.ourexists.omes.device.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Getter
@Setter
@Accessors(chain = true)
@TableName("r_dg_equip_mat")
public class EquipMat {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String dgId;

    private String equipId;

    private String matCode;

    private BigDecimal maxCapacity;
}
