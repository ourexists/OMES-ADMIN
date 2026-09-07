/*
 * Copyright (c) 2026.
 */
package com.ourexists.omes.device.service;

import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.device.model.WorkshopConfigMeta2dDto;
import com.ourexists.omes.device.pojo.WorkshopConfigMeta2d;

import java.util.List;

public interface WorkshopConfigMeta2dService extends IMyBatisPlusService<WorkshopConfigMeta2d> {

    void addOrUpdate(WorkshopConfigMeta2dDto dto);

    void delete(List<String> ids);

    List<WorkshopConfigMeta2d> queryByWorkshop(List<String> workshopIds);

    WorkshopConfigMeta2d queryByWorkshop(String workshopId);
}

