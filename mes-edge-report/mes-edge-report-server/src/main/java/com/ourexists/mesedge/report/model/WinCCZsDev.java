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
@TableName("t_wincc_zs_dev")
public class WinCCZsDev  extends EraEntity {

    private Integer csdDosingPump1;
    private Integer csdDosingPump2;
    private Integer csdDosingPump3;
    private Integer csdHyPump1;
    private Integer csdHyPump3;
    private Integer csdHyPump4;
    private Integer csdHyPump5;
    private Integer csdHyPump20;
    private Integer csdHyPump21;
    private Integer csdJbq1;
    private Integer csdJbq2;
    private Integer csdTsPump1;
    private Integer csdTsPump2;
    private Integer csdTsPump3;
    private Integer csdTsPump4;
    private Integer csdUnloadingPump;
    private Integer nacioDosingPump1;
    private Integer nacioDosingPump2;
    private Integer nacioUnloadingPump;

    private Date startTime;
    private Date endTime;
    private Date execTime;

    public static WinCCZsDevDto covert(WinCCZsDev source) {
        if (source == null) {
            return null;
        }
        WinCCZsDevDto target = new WinCCZsDevDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<WinCCZsDevDto> covert(List<WinCCZsDev> sources) {
        List<WinCCZsDevDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static <T extends WinCCZsDevDto> WinCCZsDev wrap(T source) {
        WinCCZsDev target = new WinCCZsDev();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends WinCCZsDevDto> List<WinCCZsDev> wrap(List<T> sources) {
        List<WinCCZsDev> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }
}
