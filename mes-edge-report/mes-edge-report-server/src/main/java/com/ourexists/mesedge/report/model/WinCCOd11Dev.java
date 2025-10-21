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
@TableName("t_wincc_od11_dev")
public class WinCCOd11Dev extends EraEntity {
    private Integer od11ExcessSludgePump;
    private Integer od11FillerPusher1;
    private Integer od11FillerPusher2;
    private Integer od11FillerPusher3;
    private Integer od11FillerPusher4;
    private Integer od11NitrifyingLiquidRefluxPump1;
    private Integer od11NitrifyingLiquidRefluxPump2;
    private Integer od11NitrifyingLiquidRefluxPump3;
    private Integer od11SludgeReturnPump1;
    private Integer od11SludgeReturnPump2;
    private Integer od11SludgeReturnPump3;
    private Integer od11SubmersibleMixer1;
    private Integer od11SubmersibleMixer2;
    private Integer od11SubmersibleMixer3;
    private Integer od11SubmersibleMixer4;
    private Integer od11SubmersibleMixer5;
    private Integer od11SuctionDredge;
    private Date startTime;
    private Date endTime;
    private Date execTime;
    public static WinCCOd11DevDto covert(WinCCOd11Dev source) {
        if (source == null) {
            return null;
        }
        WinCCOd11DevDto target = new WinCCOd11DevDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<WinCCOd11DevDto> covert(List<WinCCOd11Dev> sources) {
        List<WinCCOd11DevDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static <T extends WinCCOd11DevDto> WinCCOd11Dev wrap(T source) {
        WinCCOd11Dev target = new WinCCOd11Dev();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends WinCCOd11DevDto> List<WinCCOd11Dev> wrap(List<T> sources) {
        List<WinCCOd11Dev> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }
}
