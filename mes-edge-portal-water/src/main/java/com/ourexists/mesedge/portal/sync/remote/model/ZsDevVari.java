package com.ourexists.mesedge.portal.sync.remote.model;

import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.mesedge.portal.sync.remote.WinCCVari;
import com.ourexists.mesedge.report.model.WinCCZsDevDto;
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
public class ZsDevVari extends EraEntity {

    @WinCCVari("CSD_DosingPumpA")
    private Integer csdDosingPump1;

    @WinCCVari("CSD_DosingPumpB")
    private Integer csdDosingPump2;

    @WinCCVari("CSD_DosingPumpC")
    private Integer csdDosingPump3;

    @WinCCVari("CSD_UnloadingPump")
    private Integer csdUnloadingPump;

    @WinCCVari("ZS_RecyclePump1")
    private Integer csdHyPump1;

    @WinCCVari("ZS_RecyclePump3")
    private Integer csdHyPump3;

    @WinCCVari("ZS_RecyclePump4")
    private Integer csdHyPump4;

    @WinCCVari("ZS_RecyclePump5")
    private Integer csdHyPump5;

    @WinCCVari("ZS_RecyclePump20")
    private Integer csdHyPump20;

    @WinCCVari("ZS_RecyclePump21")
    private Integer csdHyPump21;

    @WinCCVari("ZS_Blender1")
    private Integer csdJbq1;

    @WinCCVari("ZS_Blender2")
    private Integer csdJbq2;

    @WinCCVari("ZS_LiftPump1")
    private Integer csdTsPump1;

    @WinCCVari("ZS_LiftPump2")
    private Integer csdTsPump2;

    @WinCCVari("ZS_LiftPump3")
    private Integer csdTsPump3;

    @WinCCVari("ZS_LiftPump4")
    private Integer csdTsPump4;

    @WinCCVari("NaCIO_DosingPumpA")
    private Integer nacioDosingPump1;

    @WinCCVari("NaCIO_DosingPumpB")
    private Integer nacioDosingPump2;

    @WinCCVari("NaCIO_UnloadingPump")
    private Integer nacioUnloadingPump;

    public static WinCCZsDevDto covert(ZsDevVari source) {
        if (source == null) {
            return null;
        }
        WinCCZsDevDto target = new WinCCZsDevDto();
        BeanUtils.copyProperties(source, target);
        target.setExecTime(new Date());
        return target;
    }

    public static List<WinCCZsDevDto> covert(List<ZsDevVari> sources) {
        List<WinCCZsDevDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static <T extends WinCCZsDevDto> ZsDevVari wrap(T source) {
        ZsDevVari target = new ZsDevVari();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends WinCCZsDevDto> List<ZsDevVari> wrap(List<T> sources) {
        List<ZsDevVari> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }
}
