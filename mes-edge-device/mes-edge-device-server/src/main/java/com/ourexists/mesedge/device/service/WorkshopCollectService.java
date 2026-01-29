/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.device.model.WorkshopCollectDto;
import com.ourexists.mesedge.device.model.WorkshopCollectPageQuery;
import com.ourexists.mesedge.device.pojo.WorkshopCollect;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface WorkshopCollectService extends IMyBatisPlusService<WorkshopCollect> {

    Page<WorkshopCollect> selectByPage(WorkshopCollectPageQuery dto);

    void addOrUpdate(WorkshopCollectDto dto);

    void delete(List<String> ids);

    List<WorkshopCollect> queryByWorkshop(List<String> workshopIds);

    WorkshopCollect queryByWorkshop(String workshopId);
}
