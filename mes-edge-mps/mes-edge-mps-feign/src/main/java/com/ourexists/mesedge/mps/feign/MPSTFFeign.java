/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.feign;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.mps.enums.MPSTFStatusEnum;
import org.springframework.web.bind.annotation.RequestParam;

public interface MPSTFFeign {

    JsonResponseEntity<Boolean> updateStatus(@RequestParam String id, @RequestParam MPSTFStatusEnum mpstfStatusEnum);
}
