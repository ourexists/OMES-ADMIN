/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service;

import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.device.model.WorkshopAssignDto;
import com.ourexists.mesedge.device.pojo.WorkshopAssign;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface WorkshopAssignService extends IMyBatisPlusService<WorkshopAssign> {
    void assign(WorkshopAssignDto workshopAssignDto);
}
