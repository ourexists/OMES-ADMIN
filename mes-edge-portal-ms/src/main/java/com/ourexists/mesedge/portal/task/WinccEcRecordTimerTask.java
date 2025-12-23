package com.ourexists.mesedge.portal.task;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.ec.feign.EcAttrFeign;
import com.ourexists.mesedge.ec.feign.EcRecordFeign;
import com.ourexists.mesedge.ec.model.EcAttrDto;
import com.ourexists.mesedge.ec.model.EcAttrPageQuery;
import com.ourexists.mesedge.ec.model.EcRecordDto;
import com.ourexists.mesedge.portal.wincc.WinccApi;
import com.ourexists.mesedge.portal.wincc.WinccVariable;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component("WinccEcRecord")
public class WinccEcRecordTimerTask extends TimerTask {

    @Autowired
    private EcAttrFeign ecAttrFeign;

    @Autowired
    private EcRecordFeign ecRecordFeign;

    @Autowired
    private WinccApi winccApi;

    @Override
    public void doRun() {
        EcAttrPageQuery query = new EcAttrPageQuery();
        query.setRequirePage(false);
        try {
            List<EcAttrDto> ecAttrDtos = RemoteHandleUtils.getDataFormResponse(ecAttrFeign.selectByPage(query));
            if (CollectionUtils.isEmpty(ecAttrDtos)) {
                return;
            }
            List<WinccVariable> r = covert(ecAttrDtos);
            r = winccApi.pullTags(r);
            List<List<EcRecordDto>> list = new ArrayList<>();
            list.add(warp(r));
            ecRecordFeign.addBatch(list);
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static WinccVariable covert(EcAttrDto source) {
        if (source == null) {
            return null;
        }
        WinccVariable target = new WinccVariable();
        target.setAttrId(source.getId());
        target.setAttrName(source.getMap());
        return target;
    }

    public static List<WinccVariable> covert(List<EcAttrDto> sources) {
        List<WinccVariable> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (EcAttrDto source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static EcRecordDto warp(WinccVariable source) {
        if (source == null) {
            return null;
        }
        EcRecordDto target = new EcRecordDto();
        target.setAttrId(source.getAttrId());
        target.setAttrVal(source.getVal().toString());
        return target;
    }

    public static List<EcRecordDto> warp(List<WinccVariable> sources) {
        List<EcRecordDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (WinccVariable source : sources) {
                targets.add(warp(source));
            }
        }
        return targets;
    }
}
