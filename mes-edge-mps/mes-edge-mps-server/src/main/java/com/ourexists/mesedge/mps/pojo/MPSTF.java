/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.pojo;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import com.ourexists.mesedge.mps.model.MPSTFDto;
import com.ourexists.mesedge.mps.model.MPSTFVo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("r_mps_tf")
public class MPSTF extends MainEntity {

    private String mpsId;

    private String selfCode;

    private String name;

    private Integer status;

    private String mapDb;

    private String mapOffset;

    private String property;

    private Long duration;

    private String pre;

    private String nex;

    private Date startTime;

    private Date endTime;

    private Double startTemperature;

    private Double endTemperature;

    private Double temperature;

    private String moCode;

    private Integer type;

    public static MPSTFVo covert(MPSTF source) {
        if (source == null) {
            return null;
        }
        MPSTFVo target = new MPSTFVo();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static List<MPSTFVo> covert(List<MPSTF> sources) {
        List<MPSTFVo> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static <T extends MPSTFDto> MPSTF wrap(T source) {
        MPSTF target = new MPSTF();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static <T extends MPSTFDto> List<MPSTF> wrap(List<T> sources) {
        List<MPSTF> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }

    public static <T extends MPSTFDto> List<MPSTF> wrap(List<T> sources, String mpsId, String moCode) {
        return wrap(sources, mpsId, moCode, false);
    }


    public static <T extends MPSTFDto> List<MPSTF> wrap(List<T> sources, String mpsId, String moCode, boolean clearId) {
        List<MPSTF> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                MPSTF mpstf = wrap(source);
                mpstf.setMpsId(mpsId);
                mpstf.setMoCode(moCode);
                if (clearId) {
                    mpstf.setId(null);
                }
                targets.add(mpstf);
            }
        }
        return targets;
    }

}
