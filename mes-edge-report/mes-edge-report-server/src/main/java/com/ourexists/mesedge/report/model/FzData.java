package com.ourexists.mesedge.report.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("fz_data")
public class FzData {

    private Integer no;

    private Date rq;

    private Integer line;

    private String pNo;

    private String bh;

    private String oper;

    private String stTime;

    private String endTime;

    private String pf;

    public static FzDataDto covert(FzData source) {
        if (source == null) {
            return null;
        }
        FzDataDto target = new FzDataDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<FzDataDto> covert(List<FzData> sources) {
        List<FzDataDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static <T extends FzDataDto> FzData wrap(T source) {
        FzData target = new FzData();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends FzDataDto> List<FzData> wrap(List<T> sources) {
        List<FzData> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }

}
