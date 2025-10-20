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

    private Boolean od20ExcessSludgePump;
    private Boolean od20FillerPusher1;
    private Boolean od20FillerPusher2;
    private Boolean od20FillerPusher3;
    private Boolean od20FillerPusher4;
    private Boolean od20NitrifyingLiquidRefluxPump1;
    private Boolean od20NitrifyingLiquidRefluxPump2;
    private Boolean od20NitrifyingLiquidRefluxPump3;
    private Boolean od20SludgeReturnPump1;
    private Boolean od20SludgeReturnPump2;
    private Boolean od20SludgeReturnPump3;
    private Boolean od20SubmersibleMixer1;
    private Boolean od20SubmersibleMixer2;
    private Boolean od20SubmersibleMixer3;
    private Boolean od20SubmersibleMixer4;
    private Boolean od20SubmersibleMixer5;
    private Boolean od20SubmersibleMixer6;
    private Boolean od20SubmersibleMixer7;
    private Boolean od20SubmersibleMixer8;
    private Boolean od20SubmersibleMixer9;
    private Boolean od20SuctionDredge;
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
