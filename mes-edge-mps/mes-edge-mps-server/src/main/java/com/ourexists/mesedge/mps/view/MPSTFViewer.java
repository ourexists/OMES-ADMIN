/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.view;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.mps.enums.MPSTFStatusEnum;
import com.ourexists.mesedge.mps.feign.MPSTFFeign;
import com.ourexists.mesedge.mps.service.MPSTFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MPSTFViewer implements MPSTFFeign {

    @Autowired
    private MPSTFService mpstfService;

    @Override
    public JsonResponseEntity<Boolean> updateStatus(String id, MPSTFStatusEnum mpstfStatusEnum) {
        mpstfService.updateStatus(id, mpstfStatusEnum);
        return JsonResponseEntity.success(true);
    }
}
