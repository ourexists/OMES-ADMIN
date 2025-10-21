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
@TableName("t_wincc_od20_dev")
public class WinCCOd20Dev extends EraEntity {

    private Integer od20ExcessSludgePump;
    private Integer od20FillerPusher1;
    private Integer od20FillerPusher2;
    private Integer od20FillerPusher3;
    private Integer od20FillerPusher4;
    private Integer od20NitrifyingLiquidRefluxPump1;
    private Integer od20NitrifyingLiquidRefluxPump2;
    private Integer od20NitrifyingLiquidRefluxPump3;
    private Integer od20SludgeReturnPump1;
    private Integer od20SludgeReturnPump2;
    private Integer od20SludgeReturnPump3;
    private Integer od20SubmersibleMixer1;
    private Integer od20SubmersibleMixer2;
    private Integer od20SubmersibleMixer3;
    private Integer od20SubmersibleMixer4;
    private Integer od20SubmersibleMixer5;
    private Integer od20SubmersibleMixer6;
    private Integer od20SubmersibleMixer7;
    private Integer od20SubmersibleMixer8;
    private Integer od20SubmersibleMixer9;
    private Integer od20SuctionDredge;
    private Date startTime;
    private Date endTime;
    private Date execTime;

    public static WinCCOd20DevDto covert(WinCCOd20Dev source) {
        if (source == null) {
            return null;
        }
        WinCCOd20DevDto target = new WinCCOd20DevDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<WinCCOd20DevDto> covert(List<WinCCOd20Dev> sources) {
        List<WinCCOd20DevDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static <T extends WinCCOd20DevDto> WinCCOd20Dev wrap(T source) {
        WinCCOd20Dev target = new WinCCOd20Dev();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends WinCCOd20DevDto> List<WinCCOd20Dev> wrap(List<T> sources) {
        List<WinCCOd20Dev> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }
}
