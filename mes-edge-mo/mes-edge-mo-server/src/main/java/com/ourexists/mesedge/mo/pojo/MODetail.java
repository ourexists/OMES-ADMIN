/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mo.pojo;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import com.ourexists.mesedge.mo.model.MODetailDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("r_mo_d")
public class MODetail extends MainEntity {

    private String mcode;

    private String matId;

    private String matName;

    private String matCode;

    private BigDecimal matNum;

    private Integer priority;

    private String devNo;

    private String devName;

    private String dgCode;

    private String dgName;

    @TableLogic
    private Boolean delBit;

    public static MODetailDto covert(MODetail source) {
        if (source == null) {
            return null;
        }
        MODetailDto target = new MODetailDto();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static List<MODetailDto> covert(List<MODetail> sources) {
        List<MODetailDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (MODetail source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }


    public static MODetail wrap(MODetailDto source) {
        MODetail target = new MODetail();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static List<MODetail> wrap(List<MODetailDto> sources) {
        List<MODetail> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (MODetailDto source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }

    public static List<MODetail> wrap(List<MODetailDto> sources, String mcode) {
        List<MODetail> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (MODetailDto source : sources) {
                source.setMcode(mcode);
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
