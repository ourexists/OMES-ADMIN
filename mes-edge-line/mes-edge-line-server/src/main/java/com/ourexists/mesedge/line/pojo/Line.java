/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.line.pojo;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.mesedge.line.model.LineDto;
import com.ourexists.mesedge.line.model.LineVo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_line")
public class Line extends EraEntity {

    private String selfCode;

    private String name;

    private BigDecimal throughput;

    private Integer stepInterval;

    private Integer type;

    private Integer mapDb;

    private String mapOffset;

    private Date syncTime;

    private Date plcTime;

    @TableLogic
    private Boolean delBit;

    public static LineVo covert(Line source) {
        if (source == null) {
            return null;
        }
        LineVo target = new LineVo();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<LineVo> covert(List<Line> sources) {
        List<LineVo> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (Line source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }


    public static Line wrap(LineDto source) {
        Line target = new Line();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<Line> wrap(List<LineDto> sources) {
        List<Line> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (LineDto source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
