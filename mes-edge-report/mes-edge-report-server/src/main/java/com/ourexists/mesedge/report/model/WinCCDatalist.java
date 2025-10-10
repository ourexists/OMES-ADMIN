package com.ourexists.mesedge.report.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
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
@TableName("t_collect_wincc_datalist")
public class WinCCDatalist extends EraEntity {

    private Float OD11AerobicTankDO;
    private Float OD11AerobicTankNitrificationAndAmmonium;
    private Float OD11AerobicTankORP1;
    private Float OD11AnaerobicTankORP;
    private Float OD11AnaerobicTankORP1;
    private Float OD11AnoxicTankDO;
    private Float OD11AnoxicTankORP;
    private Float OD11SludgeConcentrationInAerobicTank;
    private Float OD12AerobicTankDO;
    private Float OD12AerobicTankNitrificationAndAmmonium;
    private Float OD12AerobicTankORP1;
    private Float OD12AerobicTankORP2;
    private Float OD12AnaerobicTankORP;
    private Float OD12AnoxicTankDO;
    private Float OD12AnoxicTankORP;
    private Float OD12SecondaryClarifierSludge;
    private Float OD12SludgeConcentrationInAerobicTank;
    private Float OD20AerobicTankDO;
    private Float OD20AerobicTankNitrificationAndAmmonium;
    private Float OD20AerobicTankORP1;
    private Float OD20AerobicTankORP2;
    private Float OD20AnaerobicTankORP;
    private Float OD20AnoxicTankDO;
    private Float OD20AnoxicTankORP;
    private Float OD20SecondaryClarifierSludge;
    private Float OD20SludgeConcentrationInAerobicTank;
    private Float WPSflow;
    private Float WPSinletflow1;
    private Float WPSinletflow2;
    private Float WPSinletflow3;
    private Float WPSinletflow4;
    private Float WPSinletflow5;
    private Float WPSinletph;
    private Float WPSinletss;
    private Float WPSoutletflow;
    private Date startTime;
    private Date endTime;
    private Date execTime;

    public static WinCCDatalistDto covert(WinCCDatalist source) {
        if (source == null) {
            return null;
        }
        WinCCDatalistDto target = new WinCCDatalistDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<WinCCDatalistDto> covert(List<WinCCDatalist> sources) {
        List<WinCCDatalistDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static <T extends WinCCDatalistDto> WinCCDatalist wrap(T source) {
        WinCCDatalist target = new WinCCDatalist();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends WinCCDatalistDto> List<WinCCDatalist> wrap(List<T> sources) {
        List<WinCCDatalist> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }

}
