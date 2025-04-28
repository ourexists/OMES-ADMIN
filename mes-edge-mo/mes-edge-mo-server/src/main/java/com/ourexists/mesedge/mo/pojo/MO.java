/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mo.pojo;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.mesedge.mo.model.MODto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_mo")
public class MO extends EraEntity {

    private String selfCode;

    private Integer status;

    private String productId;

    private String productName;

    private String productCode;

    private Integer num;

    private BigDecimal weight;

    private String lineCode;

    private Integer surplus;

    private Date execTime;

    @TableLogic
    private Boolean delBit;

    private Integer source;

    private String sourceId;

    public static MODto covert(MO source) {
        if (source == null) {
            return null;
        }
        MODto target = new MODto();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static List<MODto> covert(List<MO> sources) {
        List<MODto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (MO source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }


    public static MO wrap(MODto source) {
        MO target = new MO();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static List<MO> wrap(List<MODto> sources) {
        List<MO> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (MODto source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
