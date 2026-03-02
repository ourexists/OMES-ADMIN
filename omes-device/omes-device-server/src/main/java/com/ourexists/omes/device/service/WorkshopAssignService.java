/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service;

import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.device.model.WorkshopAssignBatchDto;
import com.ourexists.omes.device.pojo.WorkshopAssign;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface WorkshopAssignService extends IMyBatisPlusService<WorkshopAssign> {
    void assign(WorkshopAssignBatchDto workshopAssignBatchDto);

    List<WorkshopAssign> selectWorkshopAssignByEquipId(String equipId);
}
