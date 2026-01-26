/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service;

import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.device.model.WorkshopConfigScadaDto;
import com.ourexists.mesedge.device.pojo.WorkshopConfigScada;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface WorkshopConfigScadaService extends IMyBatisPlusService<WorkshopConfigScada> {

    void addOrUpdate(WorkshopConfigScadaDto dto);

    void delete(List<String> ids);

    List<WorkshopConfigScada> queryByWorkshop(List<String> workshopIds);

    WorkshopConfigScada queryByWorkshop(String workshopId);

}
