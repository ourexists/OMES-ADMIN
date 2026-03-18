/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.pojo;

import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.omes.inspection.model.InspectRecordItemDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 巡检记录明细（附表：记录id + 巡检项 + 巡检项内容）
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_inspect_record_item")
public class InspectRecordItem extends EraEntity {

    /** 巡检记录ID（主表） */
    private String recordId;
    /** 巡检项ID（模板项主键） */
    private String itemId;
    /** 巡检项名称 */
    private String itemName;
    /** 巡检项内容：填写值/结果文本 */
    private String content;
    /** 巡检结果：0正常 1异常 */
    private Integer result;
    /** 规则分值（配置项匹配得分，未乘权重） */
    private Integer ruleScore;
    /** 该项得分（按权重/百分比权重计算后的得分） */
    private Integer score;
    /** 备注 */
    private String remark;
    /** 照片URL，多张逗号分隔 */
    private String photoUrls;

    public static InspectRecordItemDto covert(InspectRecordItem source) {
        if (source == null) return null;
        InspectRecordItemDto target = new InspectRecordItemDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<InspectRecordItemDto> covert(List<InspectRecordItem> sources) {
        List<InspectRecordItemDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (InspectRecordItem source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static <T extends InspectRecordItemDto> InspectRecordItem wrap(T source) {
        if (source == null) return null;
        InspectRecordItem target = new InspectRecordItem();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends InspectRecordItemDto> List<InspectRecordItem> wrap(List<T> sources) {
        List<InspectRecordItem> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
