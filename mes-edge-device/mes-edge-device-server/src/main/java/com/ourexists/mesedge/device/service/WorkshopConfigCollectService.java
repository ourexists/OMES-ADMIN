/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service;

import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.device.model.WorkshopConfigCollectDto;
import com.ourexists.mesedge.device.pojo.WorkshopConfigCollect;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface WorkshopConfigCollectService extends IMyBatisPlusService<WorkshopConfigCollect> {

    void addOrUpdate(WorkshopConfigCollectDto dto);

    void delete(List<String> ids);

    List<WorkshopConfigCollect> queryByWorkshop(List<String> workshopIds);

    WorkshopConfigCollect queryByWorkshop(String workshopId);

    List<WorkshopConfigCollect> queryAllConfigCollect();
}
