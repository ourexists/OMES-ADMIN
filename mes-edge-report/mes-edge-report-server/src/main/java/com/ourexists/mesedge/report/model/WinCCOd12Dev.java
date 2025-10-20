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
@TableName("t_wincc_od12_dev")
public class WinCCOd12Dev extends EraEntity {

    private Boolean od12ExcessSludgePump;
    private Boolean od12FillerPusher1;
    private Boolean od12FillerPusher2;
    private Boolean od12FillerPusher3;
    private Boolean od12FillerPusher4;
    private Boolean od12NitrifyingLiquidRefluxPump1;
    private Boolean od12NitrifyingLiquidRefluxPump2;
    private Boolean od12NitrifyingLiquidRefluxPump3;
    private Boolean od12SludgeReturnPump1;
    private Boolean od12SludgeReturnPump2;
    private Boolean od12SludgeReturnPump3;
    private Boolean od12SubmersibleMixer1;
    private Boolean od12SubmersibleMixer2;
    private Boolean od12SubmersibleMixer3;
    private Boolean od12SubmersibleMixer4;
    private Boolean od12SubmersibleMixer5;
    private Boolean od12SuctionDredge;
    private Date startTime;
    private Date endTime;
    private Date execTime;
    public static WinCCOd12DevDto covert(WinCCOd12Dev source) {
        if (source == null) {
            return null;
        }
        WinCCOd12DevDto target = new WinCCOd12DevDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<WinCCOd12DevDto> covert(List<WinCCOd12Dev> sources) {
        List<WinCCOd12DevDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static <T extends WinCCOd12DevDto> WinCCOd12Dev wrap(T source) {
        WinCCOd12Dev target = new WinCCOd12Dev();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends WinCCOd12DevDto> List<WinCCOd12Dev> wrap(List<T> sources) {
        List<WinCCOd12Dev> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }
}
