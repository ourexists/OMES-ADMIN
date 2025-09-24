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
@TableName("lm_record")
public class LmRecord {

    private Integer no;

    private Date rq;

    private String pf;

    private String bh;

    private String line;

    private String pNo;

    private String oper;

    private String tank;

    private String lm;

    private Float ll;

    private Float sj;

    private Float wc;

    private Float jd;

    private Integer fzId;

    public static LmRecordDto covert(LmRecord source) {
        if (source == null) {
            return null;
        }
        LmRecordDto target = new LmRecordDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<LmRecordDto> covert(List<LmRecord> sources) {
        List<LmRecordDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static <T extends LmRecordDto> LmRecord wrap(T source) {
        LmRecord target = new LmRecord();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends LmRecordDto> List<LmRecord> wrap(List<T> sources) {
        List<LmRecord> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }

}
